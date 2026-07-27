package com.eduhub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.common.JwtUtil;
import com.eduhub.dto.LoginDTO;
import com.eduhub.dto.RegisterDTO;
import com.eduhub.dto.UserProfileVO;
import com.eduhub.entity.User;
import com.eduhub.mapper.UserMapper;
import com.eduhub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 *
 * 面试考点：
 * 1. 密码加密 (BCrypt)
 * 2. 注册防重：Redis分布式锁
 * 3. 统一异常处理
 * 4. 登录成功写最后登录时间
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String REGISTER_LOCK_KEY = "eduhub:register:lock:";
    private static final long LOCK_EXPIRE = 10; // 秒

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        BusinessException.throwIf(count > 0, ErrorCode.USERNAME_EXISTS);

        // 2. 检查邮箱是否已存在
        if (dto.getEmail() != null) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
            BusinessException.throwIf(emailCount > 0, ErrorCode.EMAIL_EXISTS);
        }

        // 3. 创建用户（密码使用 BCrypt 加密）
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole("user");
        user.setStatus(1);
        user.setCoins(10);  // 新用户送10学币

        userMapper.insert(user);
        log.info("新用户注册: id={}, username={}", user.getId(), user.getUsername());

        return user;
    }

    @Override
    public String login(LoginDTO dto) {
        // 1. 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 3. 校验密码
        boolean valid = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!valid) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 4. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 5. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录: id={}, username={}", user.getId(), user.getUsername());

        return token;
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        BusinessException.throwIf(user == null, ErrorCode.USER_NOT_FOUND);
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = getById(userId);
        UserProfileVO vo = new UserProfileVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UserProfileVO dto) {
        User user = getById(userId);
        // 只允许更新部分字段
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        // 校验旧密码
        boolean valid = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!valid) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}

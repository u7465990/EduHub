package com.eduhub.service.impl;

import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.common.JwtUtil;
import com.eduhub.dto.LoginDTO;
import com.eduhub.dto.RegisterDTO;
import com.eduhub.dto.UserProfileVO;
import com.eduhub.entity.User;
import com.eduhub.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 单元测试
 *
 * 纯 Mockito 测试：不启动 Spring 容器，不依赖 MySQL / Redis。
 * 覆盖注册、登录、密码修改、资料更新等核心分支。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 用户服务")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static RegisterDTO registerDTO(String username, String email) {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername(username);
        dto.setPassword("123456");
        dto.setEmail(email);
        return dto;
    }

    private static User user(Long id, String username, String rawPassword, int status) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        u.setRole("user");
        u.setStatus(status);
        return u;
    }

    @Nested
    @DisplayName("注册 register")
    class Register {

        @Test
        @DisplayName("正常注册：密码被 BCrypt 加密，默认角色 user、赠送 10 学币")
        void register_success() {
            when(userMapper.selectCount(any())).thenReturn(0L);

            User saved = userService.register(registerDTO("ricky", "ricky@x.com"));

            assertThat(saved.getUsername()).isEqualTo("ricky");
            assertThat(saved.getRole()).isEqualTo("user");
            assertThat(saved.getStatus()).isEqualTo(1);
            assertThat(saved.getCoins()).isEqualTo(10);
            assertThat(saved.getNickname()).isEqualTo("ricky"); // 未传昵称时回退为用户名
            // 密码不得明文落库
            assertThat(saved.getPassword()).isNotEqualTo("123456");
            assertThat(encoder.matches("123456", saved.getPassword())).isTrue();
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("注册时传了昵称则使用昵称")
        void register_usesProvidedNickname() {
            when(userMapper.selectCount(any())).thenReturn(0L);
            RegisterDTO dto = registerDTO("ricky", null);
            dto.setNickname("杜睿哲");

            User saved = userService.register(dto);

            assertThat(saved.getNickname()).isEqualTo("杜睿哲");
        }

        @Test
        @DisplayName("用户名已存在：抛 USERNAME_EXISTS，且不写库")
        void register_usernameExists() {
            when(userMapper.selectCount(any())).thenReturn(1L);

            assertThatThrownBy(() -> userService.register(registerDTO("ricky", null)))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.USERNAME_EXISTS.getCode()));
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("邮箱已被注册：抛 EMAIL_EXISTS")
        void register_emailExists() {
            // 第一次查用户名返回 0，第二次查邮箱返回 1
            when(userMapper.selectCount(any())).thenReturn(0L, 1L);

            assertThatThrownBy(() -> userService.register(registerDTO("ricky", "dup@x.com")))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.EMAIL_EXISTS.getCode()));
            verify(userMapper, never()).insert(any(User.class));
        }
    }

    @Nested
    @DisplayName("登录 login")
    class Login {

        private LoginDTO loginDTO() {
            LoginDTO dto = new LoginDTO();
            dto.setUsername("ricky");
            dto.setPassword("123456");
            return dto;
        }

        @Test
        @DisplayName("登录成功：返回 token 并刷新最后登录时间")
        void login_success() {
            User u = user(1L, "ricky", "123456", 1);
            when(userMapper.selectOne(any())).thenReturn(u);
            when(jwtUtil.generateToken(1L, "ricky", "user")).thenReturn("token-abc");

            String token = userService.login(loginDTO());

            assertThat(token).isEqualTo("token-abc");
            assertThat(u.getLastLoginAt()).isNotNull();
            verify(userMapper).updateById(u);
        }

        @Test
        @DisplayName("用户不存在：抛 USER_NOT_FOUND")
        void login_userNotFound() {
            when(userMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> userService.login(loginDTO()))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode()));
            verify(jwtUtil, never()).generateToken(any(), any(), any());
        }

        @Test
        @DisplayName("账号被禁用：抛 ACCOUNT_DISABLED")
        void login_accountDisabled() {
            when(userMapper.selectOne(any())).thenReturn(user(1L, "ricky", "123456", 0));

            assertThatThrownBy(() -> userService.login(loginDTO()))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.ACCOUNT_DISABLED.getCode()));
        }

        @Test
        @DisplayName("密码错误：抛 PASSWORD_ERROR")
        void login_wrongPassword() {
            when(userMapper.selectOne(any())).thenReturn(user(1L, "ricky", "correct-pwd", 1));

            assertThatThrownBy(() -> userService.login(loginDTO()))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.PASSWORD_ERROR.getCode()));
            verify(jwtUtil, never()).generateToken(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("查询与资料")
    class Profile {

        @Test
        @DisplayName("getById 命中")
        void getById_found() {
            User u = user(1L, "ricky", "123456", 1);
            when(userMapper.selectById(1L)).thenReturn(u);

            assertThat(userService.getById(1L)).isSameAs(u);
        }

        @Test
        @DisplayName("getById 未命中：抛 USER_NOT_FOUND")
        void getById_notFound() {
            when(userMapper.selectById(9L)).thenReturn(null);

            assertThatThrownBy(() -> userService.getById(9L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("getProfile 拷贝用户字段")
        void getProfile_copiesFields() {
            User u = user(1L, "ricky", "123456", 1);
            u.setEmail("ricky@x.com");
            u.setCoins(30);
            when(userMapper.selectById(1L)).thenReturn(u);

            UserProfileVO vo = userService.getProfile(1L);

            assertThat(vo.getId()).isEqualTo(1L);
            assertThat(vo.getUsername()).isEqualTo("ricky");
            assertThat(vo.getEmail()).isEqualTo("ricky@x.com");
            assertThat(vo.getCoins()).isEqualTo(30);
        }

        @Test
        @DisplayName("updateProfile 只覆盖非空字段")
        void updateProfile_onlyNonNullFields() {
            User u = user(1L, "ricky", "123456", 1);
            u.setNickname("旧昵称");
            u.setPhone("13800000000");
            when(userMapper.selectById(1L)).thenReturn(u);

            UserProfileVO dto = new UserProfileVO();
            dto.setNickname("新昵称");
            userService.updateProfile(1L, dto);

            assertThat(u.getNickname()).isEqualTo("新昵称");
            assertThat(u.getPhone()).isEqualTo("13800000000"); // 未传则保留原值
            verify(userMapper).updateById(u);
        }

        @Test
        @DisplayName("changePassword 旧密码错误：抛 PASSWORD_ERROR")
        void changePassword_wrongOld() {
            when(userMapper.selectById(1L)).thenReturn(user(1L, "ricky", "123456", 1));

            assertThatThrownBy(() -> userService.changePassword(1L, "wrong", "newpass"))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.PASSWORD_ERROR.getCode()));
            verify(userMapper, never()).updateById(any(User.class));
        }

        @Test
        @DisplayName("changePassword 成功：新密码以密文写回")
        void changePassword_success() {
            User u = user(1L, "ricky", "123456", 1);
            when(userMapper.selectById(1L)).thenReturn(u);

            userService.changePassword(1L, "123456", "newpass888");

            assertThat(encoder.matches("newpass888", u.getPassword())).isTrue();
            assertThat(encoder.matches("123456", u.getPassword())).isFalse();
            verify(userMapper).updateById(u);
        }
    }
}

package com.eduhub.service;

import com.eduhub.dto.LoginDTO;
import com.eduhub.dto.RegisterDTO;
import com.eduhub.dto.UserProfileVO;
import com.eduhub.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    User register(RegisterDTO dto);

    /**
     * 用户登录，返回Token
     */
    String login(LoginDTO dto);

    /**
     * 根据ID获取用户
     */
    User getById(Long id);

    /**
     * 根据用户名获取用户
     */
    User getByUsername(String username);

    /**
     * 获取用户信息（脱敏，不含密码）
     */
    UserProfileVO getProfile(Long userId);

    /**
     * 更新用户信息
     */
    void updateProfile(Long userId, UserProfileVO dto);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}

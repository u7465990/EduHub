package com.eduhub.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息视图对象（返回给前端，不包含密码）
 */
@Data
public class UserProfileVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private Integer coins;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}

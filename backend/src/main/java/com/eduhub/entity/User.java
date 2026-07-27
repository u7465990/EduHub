package com.eduhub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField(select = false)  // 查询默认不返回密码
    private String password;

    private String nickname;
    private String email;
    private String phone;
    private String avatar;

    private String role;      // user / admin / vip
    private Integer status;    // 1启用 0禁用
    private Integer coins;     // 学币

    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

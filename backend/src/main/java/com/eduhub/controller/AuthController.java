package com.eduhub.controller;

import cn.hutool.core.bean.BeanUtil;
import com.eduhub.common.Result;
import com.eduhub.dto.LoginDTO;
import com.eduhub.dto.RegisterDTO;
import com.eduhub.dto.UserProfileVO;
import com.eduhub.entity.User;
import com.eduhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 - 登录、注册
 *
 * 面试考点：
 * 1. RESTful API 设计
 * 2. @Valid 参数校验
 * 3. JWT Token 返回
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserProfileVO> register(@Valid @RequestBody RegisterDTO dto) {
        User user = userService.register(dto);
        UserProfileVO vo = new UserProfileVO();
        BeanUtil.copyProperties(user, vo);
        return Result.success(vo, "注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        String token = userService.login(dto);
        User user = userService.getByUsername(dto.getUsername());
        UserProfileVO vo = new UserProfileVO();
        BeanUtil.copyProperties(user, vo);
        return Result.success(Map.of(
                "token", token,
                "user", vo
        ), "登录成功");
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<?> refresh() {
        // 阶段1: 直接返回成功（由拦截器自动续期）
        return Result.success();
    }
}

package com.eduhub.controller;

import com.eduhub.common.Result;
import com.eduhub.common.UserContext;
import com.eduhub.dto.RegisterDTO;
import com.eduhub.dto.UserProfileVO;
import com.eduhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile() {
        return Result.success(userService.getProfile(UserContext.getUserId()));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileVO dto) {
        userService.updateProfile(UserContext.getUserId(), dto);
        return Result.success(null, "更新成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        userService.changePassword(
                UserContext.getUserId(),
                body.get("oldPassword"),
                body.get("newPassword"));
        return Result.success(null, "密码修改成功");
    }
}

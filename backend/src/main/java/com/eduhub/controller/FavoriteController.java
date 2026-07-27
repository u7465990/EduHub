package com.eduhub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eduhub.common.PageResult;
import com.eduhub.common.Result;
import com.eduhub.common.UserContext;
import com.eduhub.entity.Question;
import com.eduhub.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "收藏管理")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "收藏题目")
    @PostMapping("/{questionId}")
    public Result<Void> add(@PathVariable Long questionId) {
        favoriteService.add(UserContext.getUserId(), questionId);
        return Result.success(null, "收藏成功");
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{questionId}")
    public Result<Void> remove(@PathVariable Long questionId) {
        favoriteService.remove(UserContext.getUserId(), questionId);
        return Result.success(null, "已取消收藏");
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/{questionId}/check")
    public Result<Map<String, Boolean>> check(@PathVariable Long questionId) {
        boolean favorited = favoriteService.isFavorited(UserContext.getUserId(), questionId);
        return Result.success(Map.of("favorited", favorited));
    }

    @Operation(summary = "我的收藏")
    @GetMapping
    public Result<PageResult<Question>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<Question> result = favoriteService.listByUser(UserContext.getUserId(), page, pageSize);
        return Result.success(PageResult.from(result));
    }
}

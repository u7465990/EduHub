package com.eduhub.controller;

import com.eduhub.common.Result;
import com.eduhub.entity.Category;
import com.eduhub.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Category> create(@RequestBody Map<String, Object> body) {
        return Result.success(categoryService.create(
                (String) body.get("name"),
                body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : null
        ));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        categoryService.update(id, body.get("name"));
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}

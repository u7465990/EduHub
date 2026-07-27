package com.eduhub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eduhub.common.PageResult;
import com.eduhub.common.Result;
import com.eduhub.common.UserContext;
import com.eduhub.dto.QuestionDTO;
import com.eduhub.dto.QuestionQueryDTO;
import com.eduhub.entity.Question;
import com.eduhub.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目控制器
 *
 * 面试考点：
 * 1. RESTful CRUD
 * 2. 分页查询封装
 * 3. 缓存策略
 */
@Tag(name = "题目管理")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "创建题目")
    @PostMapping
    public Result<Question> create(@Valid @RequestBody QuestionDTO dto) {
        return Result.success(questionService.create(dto, UserContext.getUserId()), "创建成功");
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        questionService.incrementViewCount(id); // 异步增加浏览数
        return Result.success(question);
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        return Result.success(questionService.update(id, dto));
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.success(null, "删除成功");
    }

    @Operation(summary = "分页查询题目")
    @GetMapping
    public Result<PageResult<Question>> pageQuery(QuestionQueryDTO query) {
        IPage<Question> page = questionService.pageQuery(query);
        return Result.success(PageResult.from(page));
    }

    @Operation(summary = "搜索题目")
    @GetMapping("/search")
    public Result<PageResult<Question>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<Question> result = questionService.search(keyword, page, pageSize);
        return Result.success(PageResult.from(result));
    }

    @Operation(summary = "随机题目（每日推荐）")
    @GetMapping("/random")
    public Result<List<Question>> random(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(questionService.randomQuestions(limit));
    }

    @Operation(summary = "点赞题目")
    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        questionService.like(id);
        return Result.success();
    }

    @Operation(summary = "审核题目（管理员）")
    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestParam Integer status) {
        questionService.review(id, status);
        return Result.success();
    }
}

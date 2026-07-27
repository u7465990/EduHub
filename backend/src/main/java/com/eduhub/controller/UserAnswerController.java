package com.eduhub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eduhub.common.PageResult;
import com.eduhub.common.Result;
import com.eduhub.common.UserContext;
import com.eduhub.dto.UserAnswerDTO;
import com.eduhub.entity.UserAnswer;
import com.eduhub.service.UserAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "答题记录")
@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class UserAnswerController {

    private final UserAnswerService userAnswerService;

    @Operation(summary = "提交答案")
    @PostMapping
    public Result<UserAnswer> submit(@Valid @RequestBody UserAnswerDTO dto) {
        return Result.success(userAnswerService.submit(UserContext.getUserId(), dto));
    }

    @Operation(summary = "答题历史")
    @GetMapping
    public Result<PageResult<UserAnswer>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<UserAnswer> result = userAnswerService.history(UserContext.getUserId(), page, pageSize);
        return Result.success(PageResult.from(result));
    }
}

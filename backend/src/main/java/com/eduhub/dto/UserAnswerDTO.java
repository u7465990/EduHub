package com.eduhub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交答案请求
 */
@Data
public class UserAnswerDTO {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    private String userAnswer;
}

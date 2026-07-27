package com.eduhub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 题目创建/更新请求
 */
@Data
public class QuestionDTO {

    private Long categoryId;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    private String title;
    private String answer;
    private Integer difficulty;
    private String subject;
    private String grade;
    private String type;     // choice/multi_choice/fill/essay/judge
    private String options;  // 选择题选项(JSON)
}

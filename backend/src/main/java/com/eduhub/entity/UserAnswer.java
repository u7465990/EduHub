package com.eduhub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户答题记录
 */
@Data
@TableName("user_answer")
public class UserAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long questionId;
    private String userAnswer;
    private Integer isCorrect;   // 1正确 0错误
    private Integer aiScore;     // AI评分
    private String aiFeedback;   // AI反馈

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime answeredAt;
}

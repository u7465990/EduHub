package com.eduhub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;
    private Long userId;

    private String title;
    private String content;
    private String contentImg;
    private String answer;

    private Integer difficulty;  // 1简单 2中等 3困难
    private String subject;      // 学科
    private String grade;        // 年级
    private String type;         // choice/multi_choice/fill/essay/judge

    private String options;      // JSON格式的选项（自己转）

    private Integer viewCount;
    private Integer likeCount;

    private Integer status;      // 1审核通过 0待审核 -1拒绝

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

package com.eduhub.dto;

import lombok.Data;

/**
 * 题目查询参数
 */
@Data
public class QuestionQueryDTO {

    private String keyword;      // 关键词
    private String subject;      // 学科筛选
    private Long categoryId;     // 分类筛选
    private Integer difficulty;  // 难度筛选
    private String type;         // 题型筛选
    private String grade;        // 年级筛选
    private String sortBy;       // 排序字段: view_count/like_count/created_at
    private Boolean asc;         // 是否升序

    private int page = 1;
    private int pageSize = 20;
}

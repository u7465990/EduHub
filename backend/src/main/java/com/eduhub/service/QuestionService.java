package com.eduhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eduhub.dto.QuestionDTO;
import com.eduhub.dto.QuestionQueryDTO;
import com.eduhub.entity.Question;

/**
 * 题目服务接口
 */
public interface QuestionService {

    /**
     * 创建题目
     */
    Question create(QuestionDTO dto, Long userId);

    /**
     * 根据ID获取题目（带缓存）
     */
    Question getById(Long id);

    /**
     * 更新题目
     */
    Question update(Long id, QuestionDTO dto);

    /**
     * 删除题目
     */
    void delete(Long id);

    /**
     * 分页查询题目
     */
    IPage<Question> pageQuery(QuestionQueryDTO query);

    /**
     * 全文搜索
     */
    IPage<Question> search(String keyword, int page, int pageSize);

    /**
     * 浏览量+1
     */
    void incrementViewCount(Long id);

    /**
     * 点赞
     */
    void like(Long id);

    /**
     * 随机获取题目
     */
    java.util.List<Question> randomQuestions(int limit);

    /**
     * 审核题目
     */
    void review(Long id, Integer status);
}

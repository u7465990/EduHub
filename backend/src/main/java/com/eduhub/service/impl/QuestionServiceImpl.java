package com.eduhub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.common.UserContext;
import com.eduhub.dto.QuestionDTO;
import com.eduhub.dto.QuestionQueryDTO;
import com.eduhub.entity.Question;
import com.eduhub.mapper.QuestionMapper;
import com.eduhub.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 题目服务实现
 *
 * 面试考点：
 * 1. Spring Cache (@Cacheable) + Redis 缓存
 * 2. MyBatis-Plus 分页查询
 * 3. 事务管理 (@Transactional)
 * 4. 缓存更新策略
 * 5. 防缓存穿透
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 缓存 key 前缀 */
    private static final String CACHE_PREFIX = "eduhub:question:";
    private static final String HOT_QUESTION_KEY = "eduhub:hot:questions";
    private static final long CACHE_TTL = 30; // 分钟

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Question create(QuestionDTO dto, Long userId) {
        Question question = new Question();
        BeanUtil.copyProperties(dto, question);
        question.setUserId(userId);
        question.setViewCount(0);
        question.setLikeCount(0);
        question.setStatus(0); // 默认待审核

        questionMapper.insert(question);

        // 清除热门题目缓存
        redisTemplate.delete(HOT_QUESTION_KEY);

        log.info("创建题目: id={}, userId={}", question.getId(), userId);
        return question;
    }

    @Override
    @Cacheable(value = "question", key = "#id", unless = "#result == null")
    public Question getById(Long id) {
        Question question = questionMapper.selectById(id);
        BusinessException.throwIf(question == null, ErrorCode.QUESTION_NOT_FOUND);
        return question;
    }

    /**
     * 带缓存更新的获取逻辑
     * 缓存空值防止缓存穿透
     */
    @SuppressWarnings("unchecked")
    public Question getByIdWithCache(Long id) {
        String cacheKey = CACHE_PREFIX + id;

        // 1. 查缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // 缓存空值，防止缓存穿透
            if (cached instanceof String && "NULL".equals(cached)) {
                return null;
            }
            return (Question) cached;
        }

        // 2. 查数据库
        Question question = questionMapper.selectById(id);
        if (question == null) {
            // 缓存空值，有效期短（防缓存穿透）
            redisTemplate.opsForValue().set(cacheKey, "NULL", 1, TimeUnit.MINUTES);
            return null;
        }

        // 3. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, question, CACHE_TTL, TimeUnit.MINUTES);
        return question;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "question", key = "#id")
    public Question update(Long id, QuestionDTO dto) {
        Question question = getById(id);
        BeanUtil.copyProperties(dto, question);
        question.setId(id);
        questionMapper.updateById(question);
        return question;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "question", key = "#id")
    public void delete(Long id) {
        Question question = getById(id);
        questionMapper.deleteById(id);
        log.info("删除题目: id={}", id);
    }

    @Override
    public IPage<Question> pageQuery(QuestionQueryDTO query) {
        Page<Question> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, 1)  // 只查审核通过的
                .eq(query.getSubject() != null, Question::getSubject, query.getSubject())
                .eq(query.getCategoryId() != null, Question::getCategoryId, query.getCategoryId())
                .eq(query.getDifficulty() != null, Question::getDifficulty, query.getDifficulty())
                .eq(query.getType() != null, Question::getType, query.getType())
                .eq(query.getGrade() != null, Question::getGrade, query.getGrade());

        // 排序
        if ("view_count".equals(query.getSortBy())) {
            wrapper.orderBy(true, query.getAsc() != null && query.getAsc(), Question::getViewCount);
        } else if ("like_count".equals(query.getSortBy())) {
            wrapper.orderBy(true, query.getAsc() != null && query.getAsc(), Question::getLikeCount);
        } else {
            wrapper.orderByDesc(Question::getCreatedAt); // 默认最新
        }

        return questionMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Question> search(String keyword, int page, int pageSize) {
        // 阶段1 用 MySQL 全文检索（阶段2 替换为 Elasticsearch）
        Page<Question> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, 1)
                .and(w -> w.like(Question::getTitle, keyword)
                        .or()
                        .like(Question::getContent, keyword))
                .orderByDesc(Question::getViewCount);
        return questionMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public void incrementViewCount(Long id) {
        // 用 Redis 缓存浏览数，定时刷入DB
        String key = CACHE_PREFIX + "view_count:" + id;
        redisTemplate.opsForValue().increment(key);
        // 异步刷入DB（这里简化处理，真实项目用消息队列）
        questionMapper.incrementViewCount(id);
    }

    @Override
    public void like(Long id) {
        questionMapper.selectById(id); // 检查是否存在
        questionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Question>()
                .lambda()
                .eq(Question::getId, id)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    public List<Question> randomQuestions(int limit) {
        return questionMapper.randomQuestions(limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long id, Integer status) {
        Question question = getById(id);
        question.setStatus(status);
        questionMapper.updateById(question);
        log.info("审核题目: id={}, status={}", id, status);
    }
}

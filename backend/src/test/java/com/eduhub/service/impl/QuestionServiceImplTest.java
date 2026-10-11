package com.eduhub.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.dto.QuestionDTO;
import com.eduhub.dto.QuestionQueryDTO;
import com.eduhub.entity.Question;
import com.eduhub.mapper.QuestionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * QuestionServiceImpl 单元测试
 *
 * 覆盖：创建、查询、缓存穿透防护、分页检索、浏览数累加、审核等核心逻辑。
 * 缓存相关分支通过 Mock RedisTemplate 验证读写行为，无需真实 Redis。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionServiceImpl 题目服务")
class QuestionServiceImplTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private static final String CACHE_PREFIX = "eduhub:question:";
    private static final String HOT_QUESTION_KEY = "eduhub:hot:questions";

    private static Question question(Long id) {
        Question q = new Question();
        q.setId(id);
        q.setTitle("标题");
        q.setContent("1+1=?");
        q.setType("choice");
        q.setStatus(1);
        return q;
    }

    @Nested
    @DisplayName("创建 create")
    class Create {

        @Test
        @DisplayName("初始化默认值：待审核 / 浏览数 0 / 点赞数 0，并清除热榜缓存")
        void create_initializesDefaults() {
            QuestionDTO dto = new QuestionDTO();
            dto.setTitle("算术题");
            dto.setContent("1+1=?");

            Question created = questionService.create(dto, 7L);

            assertThat(created.getUserId()).isEqualTo(7L);
            assertThat(created.getStatus()).isZero();
            assertThat(created.getViewCount()).isZero();
            assertThat(created.getLikeCount()).isZero();
            verify(questionMapper).insert(any(Question.class));
            verify(redisTemplate).delete(HOT_QUESTION_KEY);
        }
    }

    @Nested
    @DisplayName("查询 getById")
    class GetById {

        @Test
        @DisplayName("命中返回题目")
        void getById_found() {
            Question q = question(1L);
            when(questionMapper.selectById(1L)).thenReturn(q);

            assertThat(questionService.getById(1L)).isSameAs(q);
        }

        @Test
        @DisplayName("未命中抛 QUESTION_NOT_FOUND")
        void getById_notFound() {
            when(questionMapper.selectById(404L)).thenReturn(null);

            assertThatThrownBy(() -> questionService.getById(404L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.QUESTION_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("带缓存查询 getByIdWithCache（防缓存穿透）")
    class GetByIdWithCache {

        @Test
        @DisplayName("缓存命中直接返回，不回查数据库")
        void cacheHit_returnsCached() {
            Question cached = question(1L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_PREFIX + 1)).thenReturn(cached);

            assertThat(questionService.getByIdWithCache(1L)).isSameAs(cached);
            verify(questionMapper, never()).selectById(any());
        }

        @Test
        @DisplayName("缓存命中空值哨兵 NULL：直接返回 null，不回查数据库")
        void cacheHitNullSentinel_returnsNull() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_PREFIX + 2)).thenReturn("NULL");

            assertThat(questionService.getByIdWithCache(2L)).isNull();
            verify(questionMapper, never()).selectById(any());
        }

        @Test
        @DisplayName("数据库也没有：写入 NULL 哨兵（短 TTL）防穿透")
        void dbMiss_cachesNullSentinel() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_PREFIX + 3)).thenReturn(null);
            when(questionMapper.selectById(3L)).thenReturn(null);

            assertThat(questionService.getByIdWithCache(3L)).isNull();
            verify(valueOperations).set(CACHE_PREFIX + 3, "NULL", 1, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("数据库命中：写入缓存（30 分钟）")
        void dbHit_writesCache() {
            Question q = question(4L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get(CACHE_PREFIX + 4)).thenReturn(null);
            when(questionMapper.selectById(4L)).thenReturn(q);

            assertThat(questionService.getByIdWithCache(4L)).isSameAs(q);
            verify(valueOperations).set(CACHE_PREFIX + 4, q, 30, TimeUnit.MINUTES);
        }
    }

    @Nested
    @DisplayName("更新与删除")
    class UpdateAndDelete {

        @Test
        @DisplayName("更新：保留 id，覆盖传入字段")
        void update_keepsIdAndCopies() {
            Question existing = question(1L);
            existing.setContent("旧内容");
            when(questionMapper.selectById(1L)).thenReturn(existing);

            QuestionDTO dto = new QuestionDTO();
            dto.setContent("新内容");

            Question updated = questionService.update(1L, dto);

            assertThat(updated.getId()).isEqualTo(1L);
            assertThat(updated.getContent()).isEqualTo("新内容");
            verify(questionMapper).updateById(existing);
        }

        @Test
        @DisplayName("删除不存在的题目：抛 QUESTION_NOT_FOUND，不执行 delete")
        void delete_notFound() {
            when(questionMapper.selectById(9L)).thenReturn(null);

            assertThatThrownBy(() -> questionService.delete(9L))
                    .isInstanceOf(BusinessException.class);
            verify(questionMapper, never()).deleteById(any(Serializable.class));
        }

        @Test
        @DisplayName("删除成功")
        void delete_success() {
            when(questionMapper.selectById(1L)).thenReturn(question(1L));

            questionService.delete(1L);

            verify(questionMapper).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("分页与检索")
    class PagingAndSearch {

        @Test
        @DisplayName("pageQuery 走数据库分页")
        void pageQuery_delegatesToMapper() {
            Page<Question> expected = new Page<>(1, 10);
            when(questionMapper.selectPage(ArgumentMatchers.<Page<Question>>any(), any()))
                    .thenReturn(expected);

            QuestionQueryDTO query = new QuestionQueryDTO();
            query.setPage(1);
            query.setPageSize(10);

            IPage<Question> result = questionService.pageQuery(query);

            assertThat(result).isSameAs(expected);
            verify(questionMapper).selectPage(ArgumentMatchers.<Page<Question>>any(), any());
        }

        @Test
        @DisplayName("search 走数据库分页")
        void search_delegatesToMapper() {
            Page<Question> expected = new Page<>(2, 5);
            when(questionMapper.selectPage(ArgumentMatchers.<Page<Question>>any(), any()))
                    .thenReturn(expected);

            assertThat(questionService.search("勾股", 2, 5)).isSameAs(expected);
        }

        @Test
        @DisplayName("randomQuestions 透传 limit")
        void randomQuestions_passesLimit() {
            List<Question> list = List.of(question(1L), question(2L));
            when(questionMapper.randomQuestions(2)).thenReturn(list);

            assertThat(questionService.randomQuestions(2)).hasSize(2);
        }
    }

    @Nested
    @DisplayName("浏览数与点赞")
    class ViewAndLike {

        @Test
        @DisplayName("incrementViewCount：Redis 计数 + 落库")
        void incrementViewCount_updatesRedisAndDb() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            questionService.incrementViewCount(1L);

            verify(valueOperations).increment(CACHE_PREFIX + "view_count:1");
            verify(questionMapper).incrementViewCount(1L);
        }

        @Test
        @DisplayName("like：自增 like_count")
        void like_increments() {
            questionService.like(1L);

            verify(questionMapper).selectById(1L);
            verify(questionMapper).update(isNull(), any());
        }
    }

    @Nested
    @DisplayName("审核 review")
    class Review {

        @Test
        @DisplayName("审核通过：状态更新为 1 并写库")
        void review_updatesStatus() {
            Question q = question(1L);
            q.setStatus(0);
            when(questionMapper.selectById(1L)).thenReturn(q);

            questionService.review(1L, 1);

            assertThat(q.getStatus()).isEqualTo(1);
            verify(questionMapper).updateById(q);
        }
    }
}

package com.eduhub.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.entity.Favorite;
import com.eduhub.entity.Question;
import com.eduhub.mapper.FavoriteMapper;
import com.eduhub.mapper.QuestionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FavoriteServiceImpl 单元测试
 *
 * 覆盖：收藏校验（题目存在 + 防重复）、取消收藏、收藏状态判定、收藏列表分页。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteServiceImpl 收藏服务")
class FavoriteServiceImplTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Nested
    @DisplayName("添加收藏 add")
    class Add {

        @Test
        @DisplayName("题目不存在：抛 QUESTION_NOT_FOUND")
        void add_questionNotFound() {
            when(questionMapper.selectById(99L)).thenReturn(null);

            assertThatThrownBy(() -> favoriteService.add(1L, 99L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.QUESTION_NOT_FOUND.getCode()));
            verify(favoriteMapper, never()).insert(any(Favorite.class));
        }

        @Test
        @DisplayName("重复收藏：抛 DUPLICATE_FAVORITE")
        void add_duplicate() {
            when(questionMapper.selectById(1L)).thenReturn(new Question());
            when(favoriteMapper.selectCount(any())).thenReturn(1L);

            assertThatThrownBy(() -> favoriteService.add(1L, 1L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.DUPLICATE_FAVORITE.getCode()));
            verify(favoriteMapper, never()).insert(any(Favorite.class));
        }

        @Test
        @DisplayName("收藏成功：写入正确的 userId / questionId")
        void add_success() {
            when(questionMapper.selectById(1L)).thenReturn(new Question());
            when(favoriteMapper.selectCount(any())).thenReturn(0L);

            favoriteService.add(7L, 1L);

            verify(favoriteMapper).insert(ArgumentMatchers.argThat(
                    f -> f.getUserId().equals(7L) && f.getQuestionId().equals(1L)));
        }
    }

    @Nested
    @DisplayName("取消收藏 remove")
    class Remove {

        @Test
        @DisplayName("按 userId + questionId 删除")
        void remove_deletes() {
            favoriteService.remove(7L, 1L);

            verify(favoriteMapper).delete(any());
        }
    }

    @Nested
    @DisplayName("收藏状态 isFavorited")
    class IsFavorited {

        @Test
        @DisplayName("未登录（userId 为 null）：直接返回 false，不查库")
        void nullUser_returnsFalseWithoutQuery() {
            assertThat(favoriteService.isFavorited(null, 1L)).isFalse();
            verify(favoriteMapper, never()).selectCount(any());
        }

        @Test
        @DisplayName("已收藏：返回 true")
        void favorited_returnsTrue() {
            when(favoriteMapper.selectCount(any())).thenReturn(1L);

            assertThat(favoriteService.isFavorited(7L, 1L)).isTrue();
        }

        @Test
        @DisplayName("未收藏：返回 false")
        void notFavorited_returnsFalse() {
            when(favoriteMapper.selectCount(any())).thenReturn(0L);

            assertThat(favoriteService.isFavorited(7L, 1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("收藏列表 listByUser")
    class ListByUser {

        @Test
        @DisplayName("走数据库分页")
        void listByUser_delegatesToMapper() {
            Page<Question> expected = new Page<>(1, 10);
            when(questionMapper.selectPage(ArgumentMatchers.<Page<Question>>any(), any()))
                    .thenReturn(expected);

            assertThat(favoriteService.listByUser(7L, 1, 10)).isSameAs(expected);
        }
    }
}

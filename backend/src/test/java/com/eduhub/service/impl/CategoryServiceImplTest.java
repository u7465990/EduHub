package com.eduhub.service.impl;

import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.entity.Category;
import com.eduhub.mapper.CategoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CategoryServiceImpl 单元测试
 *
 * 覆盖：列表、按 id 查询、创建（根/子分类层级推导）、更新、删除（含子分类拦截）。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl 分类服务")
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private static Category category(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }

    @Test
    @DisplayName("list 返回全部（按 sortOrder 排序）")
    void list_returnsAll() {
        List<Category> list = List.of(category(1L, "数学"), category(2L, "语文"));
        when(categoryMapper.selectList(any())).thenReturn(list);

        assertThat(categoryService.list()).hasSize(2);
    }

    @Nested
    @DisplayName("查询 getById")
    class GetById {

        @Test
        @DisplayName("命中返回分类")
        void getById_found() {
            Category c = category(1L, "数学");
            when(categoryMapper.selectById(1L)).thenReturn(c);

            assertThat(categoryService.getById(1L)).isSameAs(c);
        }

        @Test
        @DisplayName("未命中抛 CATEGORY_NOT_FOUND")
        void getById_notFound() {
            when(categoryMapper.selectById(9L)).thenReturn(null);

            assertThatThrownBy(() -> categoryService.getById(9L))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND.getCode()));
        }
    }

    @Nested
    @DisplayName("创建 create")
    class Create {

        @Test
        @DisplayName("根分类（parentId 为 null）：level = 1")
        void create_root_isLevelOne() {
            categoryService.create("数学", null);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryMapper).insert(captor.capture());
            assertThat(captor.getValue().getLevel()).isEqualTo(1);
            assertThat(captor.getValue().getParentId()).isNull();
        }

        @Test
        @DisplayName("子分类（parentId 非空）：level = 2")
        void create_child_isLevelTwo() {
            categoryService.create("代数", 1L);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryMapper).insert(captor.capture());
            assertThat(captor.getValue().getLevel()).isEqualTo(2);
            assertThat(captor.getValue().getParentId()).isEqualTo(1L);
        }
    }

    @Test
    @DisplayName("update 覆盖名称")
    void update_changesName() {
        Category c = category(1L, "旧名");
        when(categoryMapper.selectById(1L)).thenReturn(c);

        categoryService.update(1L, "新名");

        assertThat(c.getName()).isEqualTo("新名");
        verify(categoryMapper).updateById(c);
    }

    @Nested
    @DisplayName("删除 delete")
    class Delete {

        @Test
        @DisplayName("存在子分类：抛异常并提示先删子分类，不执行删除")
        void delete_hasChildren_throws() {
            when(categoryMapper.selectCount(any())).thenReturn(2L);

            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOfSatisfying(BusinessException.class, ex -> {
                        assertThat(ex.getCode()).isEqualTo(ErrorCode.FAILED.getCode());
                        assertThat(ex.getMsg()).contains("请先删除子分类");
                    });
            verify(categoryMapper, never()).deleteById(any(Serializable.class));
        }

        @Test
        @DisplayName("无子分类：正常删除")
        void delete_noChildren_success() {
            when(categoryMapper.selectCount(any())).thenReturn(0L);

            categoryService.delete(1L);

            verify(categoryMapper).deleteById(1L);
        }
    }
}

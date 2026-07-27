package com.eduhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.entity.Category;
import com.eduhub.mapper.CategoryMapper;
import com.eduhub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> list() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId));
    }

    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        BusinessException.throwIf(category == null, ErrorCode.CATEGORY_NOT_FOUND);
        return category;
    }

    @Override
    public Category create(String name, Long parentId) {
        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setLevel(parentId == null ? 1 : 2);
        category.setSortOrder(0);
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public void update(Long id, String name) {
        Category category = getById(id);
        category.setName(name);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        // 检查是否有子分类
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        BusinessException.throwIf(childCount > 0, ErrorCode.FAILED, "请先删除子分类");
        categoryMapper.deleteById(id);
    }
}

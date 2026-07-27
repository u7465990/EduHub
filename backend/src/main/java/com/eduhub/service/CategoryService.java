package com.eduhub.service;

import com.eduhub.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> list();
    Category getById(Long id);
    Category create(String name, Long parentId);
    void update(Long id, String name);
    void delete(Long id);
}

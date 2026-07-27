package com.eduhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eduhub.entity.Question;

public interface FavoriteService {
    void add(Long userId, Long questionId);
    void remove(Long userId, Long questionId);
    boolean isFavorited(Long userId, Long questionId);
    IPage<Question> listByUser(Long userId, int page, int pageSize);
}

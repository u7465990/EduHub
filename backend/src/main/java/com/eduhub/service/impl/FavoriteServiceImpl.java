package com.eduhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.entity.Favorite;
import com.eduhub.entity.Question;
import com.eduhub.mapper.FavoriteMapper;
import com.eduhub.mapper.QuestionMapper;
import com.eduhub.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, Long questionId) {
        // 检查题目存在
        BusinessException.throwIf(questionMapper.selectById(questionId) == null,
                ErrorCode.QUESTION_NOT_FOUND);

        // 检查是否已收藏
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getQuestionId, questionId));
        BusinessException.throwIf(count > 0, ErrorCode.DUPLICATE_FAVORITE);

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setQuestionId(questionId);
        favoriteMapper.insert(favorite);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long userId, Long questionId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getQuestionId, questionId));
    }

    @Override
    public boolean isFavorited(Long userId, Long questionId) {
        if (userId == null) return false;
        Long count = favoriteMapper.selectCount(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getQuestionId, questionId));
        return count > 0;
    }

    @Override
    public IPage<Question> listByUser(Long userId, int page, int pageSize) {
        Page<Question> pageObj = new Page<>(page, pageSize);
        return questionMapper.selectPage(pageObj, new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question>()
                .inSql(Question::getId,
                        "SELECT question_id FROM favorite WHERE user_id = " + userId)
                .orderByDesc(Question::getCreatedAt));
    }
}

package com.eduhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduhub.dto.UserAnswerDTO;
import com.eduhub.entity.Question;
import com.eduhub.entity.UserAnswer;
import com.eduhub.mapper.QuestionMapper;
import com.eduhub.mapper.UserAnswerMapper;
import com.eduhub.service.UserAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAnswerServiceImpl implements UserAnswerService {

    private final UserAnswerMapper userAnswerMapper;
    private final QuestionMapper questionMapper;

    @Override
    public UserAnswer submit(Long userId, UserAnswerDTO dto) {
        Question question = questionMapper.selectById(dto.getQuestionId());
        if (question == null) {
            throw new com.eduhub.common.BusinessException(
                    com.eduhub.common.ErrorCode.QUESTION_NOT_FOUND);
        }

        UserAnswer ua = new UserAnswer();
        ua.setUserId(userId);
        ua.setQuestionId(dto.getQuestionId());
        ua.setUserAnswer(dto.getUserAnswer());

        // 简单判断客观题是否正确（选择题比对答案）
        if (question.getType().equals("choice") || question.getType().equals("judge")) {
            if (question.getAnswer() != null
                    && question.getAnswer().trim().equalsIgnoreCase(dto.getUserAnswer().trim())) {
                ua.setIsCorrect(1);
            } else {
                ua.setIsCorrect(0);
            }
        }

        userAnswerMapper.insert(ua);
        return ua;
    }

    @Override
    public IPage<UserAnswer> history(Long userId, int page, int pageSize) {
        return userAnswerMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserAnswer>()
                        .eq(UserAnswer::getUserId, userId)
                        .orderByDesc(UserAnswer::getAnsweredAt));
    }
}

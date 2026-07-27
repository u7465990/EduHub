package com.eduhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eduhub.dto.UserAnswerDTO;
import com.eduhub.entity.UserAnswer;

public interface UserAnswerService {
    UserAnswer submit(Long userId, UserAnswerDTO dto);
    IPage<UserAnswer> history(Long userId, int page, int pageSize);
}

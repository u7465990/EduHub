package com.eduhub.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduhub.common.BusinessException;
import com.eduhub.common.ErrorCode;
import com.eduhub.dto.UserAnswerDTO;
import com.eduhub.entity.Question;
import com.eduhub.entity.UserAnswer;
import com.eduhub.mapper.QuestionMapper;
import com.eduhub.mapper.UserAnswerMapper;
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
 * UserAnswerServiceImpl 单元测试
 *
 * 覆盖：提交答案时题目存在性校验、客观题（choice/judge）自动判分、主观题不判分、历史记录分页。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAnswerServiceImpl 答题服务")
class UserAnswerServiceImplTest {

    @Mock
    private UserAnswerMapper userAnswerMapper;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private UserAnswerServiceImpl userAnswerService;

    private static Question question(String type, String answer) {
        Question q = new Question();
        q.setId(1L);
        q.setType(type);
        q.setAnswer(answer);
        return q;
    }

    private static UserAnswerDTO answer(String value) {
        UserAnswerDTO dto = new UserAnswerDTO();
        dto.setQuestionId(1L);
        dto.setUserAnswer(value);
        return dto;
    }

    @Nested
    @DisplayName("提交答案 submit")
    class Submit {

        @Test
        @DisplayName("题目不存在：抛 QUESTION_NOT_FOUND，不落库")
        void submit_questionNotFound() {
            when(questionMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> userAnswerService.submit(7L, answer("A")))
                    .isInstanceOfSatisfying(BusinessException.class,
                            ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.QUESTION_NOT_FOUND.getCode()));
            verify(userAnswerMapper, never()).insert(any(UserAnswer.class));
        }

        @Test
        @DisplayName("选择题答对：isCorrect = 1（忽略大小写与首尾空格）")
        void submit_choiceCorrect() {
            when(questionMapper.selectById(1L)).thenReturn(question("choice", "A"));

            UserAnswer ua = userAnswerService.submit(7L, answer("  a  "));

            assertThat(ua.getIsCorrect()).isEqualTo(1);
            assertThat(ua.getUserId()).isEqualTo(7L);
            assertThat(ua.getQuestionId()).isEqualTo(1L);
            verify(userAnswerMapper).insert(ua);
        }

        @Test
        @DisplayName("选择题答错：isCorrect = 0")
        void submit_choiceWrong() {
            when(questionMapper.selectById(1L)).thenReturn(question("choice", "A"));

            assertThat(userAnswerService.submit(7L, answer("B")).getIsCorrect()).isZero();
        }

        @Test
        @DisplayName("判断题答对：isCorrect = 1")
        void submit_judgeCorrect() {
            when(questionMapper.selectById(1L)).thenReturn(question("judge", "true"));

            assertThat(userAnswerService.submit(7L, answer("TRUE")).getIsCorrect()).isEqualTo(1);
        }

        @Test
        @DisplayName("非客观题（essay）：不自动判分，isCorrect 保持 null")
        void submit_subjective_noAutoGrade() {
            when(questionMapper.selectById(1L)).thenReturn(question("essay", "参考答案"));

            assertThat(userAnswerService.submit(7L, answer("我的作答")).getIsCorrect()).isNull();
        }
    }

    @Nested
    @DisplayName("答题历史 history")
    class History {

        @Test
        @DisplayName("走数据库分页")
        void history_delegatesToMapper() {
            Page<UserAnswer> expected = new Page<>(1, 10);
            when(userAnswerMapper.selectPage(ArgumentMatchers.<Page<UserAnswer>>any(), any()))
                    .thenReturn(expected);

            assertThat(userAnswerService.history(7L, 1, 10)).isSameAs(expected);
        }
    }
}

package com.eduhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduhub.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 题目 Mapper
 *
 * 面试考点：
 * 1. MyBatis-Plus 自动CRUD
 * 2. @Select/@Update 自定义SQL
 * 3. 全文检索
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * MySQL全文检索（阶段2会替换为Elasticsearch）
     */
    @Select("SELECT * FROM question WHERE MATCH(title, content) AGAINST(#{keyword} IN BOOLEAN MODE) " +
            "AND status = 1 ORDER BY view_count DESC LIMIT #{limit}")
    List<Question> fullTextSearch(@Param("keyword") String keyword, @Param("limit") int limit);

    /**
     * 随机获取题目（用于每日推荐）
     */
    @Select("SELECT * FROM question WHERE status = 1 ORDER BY RAND() LIMIT #{limit}")
    List<Question> randomQuestions(@Param("limit") int limit);

    /**
     * 更新浏览数
     */
    @Update("UPDATE question SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);
}

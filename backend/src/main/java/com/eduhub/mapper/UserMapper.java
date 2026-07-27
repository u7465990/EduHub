package com.eduhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduhub.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * MyBatis-Plus 提供基础的 CRUD 方法，无需写 XML
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

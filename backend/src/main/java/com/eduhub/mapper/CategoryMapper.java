package com.eduhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduhub.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}

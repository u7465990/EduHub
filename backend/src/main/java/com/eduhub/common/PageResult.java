package com.eduhub.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一分页结果
 * 面试考点：MyBatis-Plus 分页设计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private long page;       // 当前页码
    private long pageSize;   // 每页条数
    private long total;      // 总条数
    private long totalPages; // 总页数
    private List<T> list;    // 数据列表

    /**
     * 从 MyBatis-Plus IPage 构建
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                page.getRecords()
        );
    }
}

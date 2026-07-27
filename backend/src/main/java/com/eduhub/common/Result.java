package com.eduhub.common;

import lombok.Data;

/**
 * 统一API响应结果
 * 面试考点：统一返回格式、泛型设计
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private Long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 成功 ====================

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = ErrorCode.SUCCESS.getCode();
        r.message = ErrorCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> r = success(data);
        r.message = message;
        return r;
    }

    // ==================== 失败 ====================

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> Result<T> error(ErrorCode errorCode, String message) {
        return error(errorCode.getCode(), message);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(String message) {
        return error(ErrorCode.FAILED.getCode(), message);
    }

    // ==================== 便捷方法 ====================

    public boolean isSuccess() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }
}

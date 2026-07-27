package com.eduhub.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 * 面试考点：枚举定义、错误码规范设计
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // ========== 通用错误码 (1xxxx) ==========
    SUCCESS(10000, "操作成功"),
    FAILED(10001, "操作失败"),
    PARAM_ERROR(10002, "参数错误"),
    PARAM_MISSING(10003, "缺少必要参数"),
    METHOD_NOT_SUPPORTED(10004, "请求方法不支持"),
    MEDIA_TYPE_ERROR(10005, "媒体类型不支持"),

    // ========== 用户/认证错误 (2xxxx) ==========
    UNAUTHORIZED(20001, "未登录或Token已过期"),
    FORBIDDEN(20002, "权限不足"),
    USER_NOT_FOUND(20003, "用户不存在"),
    PASSWORD_ERROR(20004, "密码错误"),
    USERNAME_EXISTS(20005, "用户名已存在"),
    EMAIL_EXISTS(20006, "邮箱已被注册"),
    ACCOUNT_DISABLED(20007, "账号已被禁用"),
    TOKEN_INVALID(20008, "Token无效"),
    TOKEN_EXPIRED(20009, "Token已过期"),

    // ========== 业务错误 (3xxxx) ==========
    QUESTION_NOT_FOUND(30001, "题目不存在"),
    CATEGORY_NOT_FOUND(30002, "分类不存在"),
    DUPLICATE_FAVORITE(30003, "已收藏该题目"),
    COINS_INSUFFICIENT(30004, "学币不足"),
    QUESTION_REJECTED(30005, "题目审核未通过"),

    // ========== 系统错误 (5xxxx) ==========
    INTERNAL_ERROR(50001, "服务器内部错误"),
    DB_ERROR(50002, "数据库异常"),
    CACHE_ERROR(50003, "缓存异常"),
    RATE_LIMIT(50004, "请求过于频繁，请稍后再试"),
    SERVICE_UNAVAILABLE(50005, "服务暂不可用");

    private final int code;
    private final String message;

    /**
     * 根据code获取枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode ec : values()) {
            if (ec.code == code) return ec;
        }
        return FAILED;
    }
}

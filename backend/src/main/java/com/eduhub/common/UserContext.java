package com.eduhub.common;

/**
 * 用户上下文 - 存储当前登录用户信息
 *
 * 面试考点：ThreadLocal 线程隔离
 * 每个请求的线程都有自己的用户信息副本，线程安全
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> CONTEXT = new ThreadLocal<>();

    public static void set(UserInfo user) {
        CONTEXT.set(user);
    }

    public static UserInfo get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        UserInfo user = get();
        return user != null ? user.getUserId() : null;
    }

    public static String getUsername() {
        UserInfo user = get();
        return user != null ? user.getUsername() : null;
    }

    public static String getRole() {
        UserInfo user = get();
        return user != null ? user.getRole() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 用户信息内部类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
    }
}

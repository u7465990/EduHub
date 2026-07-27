package com.eduhub.interceptor;

import com.eduhub.common.ErrorCode;
import com.eduhub.common.JwtUtil;
import com.eduhub.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 *
 * 面试考点：
 * 1. 拦截器 vs 过滤器 (Interceptor vs Filter)
 * 2. Token 验证流程
 * 3. ThreadLocal 用户上下文
 * 4. 白名单机制
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 请求直接放行（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // 没有 Token，返回 401
        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            writeUnauthorized(response, "Missing or invalid token");
            return false;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        // Token 无效或过期
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "Token expired or invalid");
            return false;
        }

        // 解析用户信息，放入 ThreadLocal
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        UserContext.set(new UserContext.UserInfo(userId, username, role));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 返回 401 未授权
     */
    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");
        try {
            String json = String.format(
                    "{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}",
                    ErrorCode.UNAUTHORIZED.getCode(), message, System.currentTimeMillis()
            );
            response.getWriter().write(json);
        } catch (Exception ignored) {
        }
    }
}

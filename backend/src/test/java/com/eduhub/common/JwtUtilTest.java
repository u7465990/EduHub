package com.eduhub.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil 单元测试
 *
 * 覆盖：Token 生成/解析往返、有效性校验（含被篡改与已过期场景）。
 * 直接用真实密钥构造 JwtUtil，不启动 Spring 容器。
 */
@DisplayName("JwtUtil Token 工具")
class JwtUtilTest {

    private static final String SECRET =
            "EduHub2024SecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong!";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 86_400_000L); // 24 小时
    }

    @Test
    @DisplayName("生成后可解析回原始声明")
    void generateAndParse_roundTrip() {
        String token = jwtUtil.generateToken(42L, "ricky", "admin");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("ricky");
        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("自签发的 Token 校验通过")
    void validateToken_validToken() {
        assertThat(jwtUtil.validateToken(jwtUtil.generateToken(1L, "u", "user"))).isTrue();
    }

    @Test
    @DisplayName("乱码 Token 校验失败")
    void validateToken_malformed() {
        assertThat(jwtUtil.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    @DisplayName("被篡改的 Token 校验失败（签名不匹配）")
    void validateToken_tamperedSignature() {
        String token = jwtUtil.generateToken(1L, "u", "user");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("已过期的 Token 校验失败")
    void validateToken_expired() {
        JwtUtil expired = new JwtUtil(SECRET, -1_000L); // 签发即过期
        String token = expired.generateToken(1L, "u", "user");

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("getExpirationTime 返回正数剩余有效期")
    void getExpirationTime_positive() {
        assertThat(jwtUtil.getExpirationTime(jwtUtil.generateToken(1L, "u", "user")))
                .isPositive();
    }
}

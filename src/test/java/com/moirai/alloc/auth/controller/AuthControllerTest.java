package com.moirai.alloc.auth.controller;

import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        ServletTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String validRefreshToken;
    private String csrfToken;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        rawPassword = "password123!";
        testUser = User.builder()
                .loginId("testuser")
                .password(passwordEncoder.encode(rawPassword))
                .userName("테스트유저")
                .email("test@test.com")
                .phone("010-1234-5678")
                .auth(User.Auth.USER)
                .build();
        userRepository.save(testUser);

        // Refresh Token 생성 및 Redis 저장
        validRefreshToken = jwtTokenProvider.createRefreshToken(testUser.getUserId());
        refreshTokenStore.save(
                String.valueOf(testUser.getUserId()),
                validRefreshToken,
                jwtTokenProvider.getRefreshExpSeconds()
        );

        csrfToken = "test-csrf-token";
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 Access Token 재발급 성공")
    void refresh_success() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", validRefreshToken), new Cookie("csrfToken", csrfToken))
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("accessToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("refreshToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("csrfToken="))));
    }

    @Test
    @DisplayName("Refresh Token 쿠키 없이 요청 시 400 반환")
    void refresh_noCookie_fail() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .header("X-CSRF-Token", csrfToken)
                        .cookie(new Cookie("csrfToken", csrfToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("CSRF 토큰 누락 시 403 반환")
    void refresh_csrfMissing_fail() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", validRefreshToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CSRF_FORBIDDEN"));
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 요청 시 400 반환")
    void refresh_invalidToken_fail() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token"), new Cookie("csrfToken", csrfToken))
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Redis에 없는 Refresh Token으로 요청 시 400 반환")
    void refresh_tokenNotInRedis_fail() throws Exception {
        // Redis에 수동으로 다른 값 저장해서 불일치 유도
        refreshTokenStore.save(
                String.valueOf(testUser.getUserId()),
                "manually-different-token",
                jwtTokenProvider.getRefreshExpSeconds()
        );

        // 기존 validRefreshToken으로 요청 → Redis 값과 불일치
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", validRefreshToken), new Cookie("csrfToken", csrfToken))
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 성공 시 쿠키 3종 발급")
    void login_setsCookies_success() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "loginId", testUser.getLoginId(),
                "password", rawPassword
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("accessToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("refreshToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("csrfToken="))));
    }

    @Test
    @DisplayName("로그아웃 시 쿠키 3종 삭제")
    void logout_clearsCookies_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("refreshToken", validRefreshToken), new Cookie("csrfToken", csrfToken))
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("accessToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("refreshToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("csrfToken="))))
                .andExpect(header().stringValues("Set-Cookie", org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.containsString("Max-Age=0"))));
    }
}

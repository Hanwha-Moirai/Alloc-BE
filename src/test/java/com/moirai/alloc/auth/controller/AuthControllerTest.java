package com.moirai.alloc.auth.controller;

import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;


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

    private User testUser;
    private String validRefreshToken;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testUser = User.builder()
                .loginId("testuser")
                .password("encodedPassword")
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
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 Access Token 재발급 성공")
    void refresh_success() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", validRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Refresh Token 쿠키 없이 요청 시 400 반환")
    void refresh_noCookie_fail() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 요청 시 400 반환")
    void refresh_invalidToken_fail() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", "invalid-token")))
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
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", validRefreshToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

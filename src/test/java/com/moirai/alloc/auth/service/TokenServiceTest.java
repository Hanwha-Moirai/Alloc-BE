package com.moirai.alloc.auth.service;

import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String validRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .password("encodedPassword")
                .userName("테스트유저")
                .email("tokentest@test.com")
                .phone("010-1234-5678")
                .auth(User.Auth.USER)
                .build();
        userRepository.save(testUser);

        validRefreshToken = jwtTokenProvider.createRefreshToken(testUser.getUserId());
        refreshTokenStore.save(
                String.valueOf(testUser.getUserId()),
                validRefreshToken,
                jwtTokenProvider.getRefreshExpSeconds()
        );
    }

    @Nested
    @DisplayName("Access Token 재발급")
    class Refresh {

        @Test
        @DisplayName("유효한 Refresh Token으로 Access Token 재발급 성공")
        void refresh_success() {
            AuthResponse response = tokenService.refresh(validRefreshToken);

            assertThat(response.accessToken()).isNotNull();
            assertThat(response.accessToken()).isNotEmpty();
        }

        @Test
        @DisplayName("null Refresh Token으로 요청 시 예외 발생")
        void refresh_nullToken_throwsException() {
            assertThatThrownBy(() -> tokenService.refresh(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh Token이 유효하지 않습니다.");
        }

        @Test
        @DisplayName("빈 문자열 Refresh Token으로 요청 시 예외 발생")
        void refresh_emptyToken_throwsException() {
            assertThatThrownBy(() -> tokenService.refresh(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh Token이 유효하지 않습니다.");
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로 요청 시 예외 발생")
        void refresh_invalidToken_throwsException() {
            assertThatThrownBy(() -> tokenService.refresh("invalid-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh Token이 유효하지 않습니다.");
        }

        @Test
        @DisplayName("Redis에 없는 Refresh Token으로 요청 시 예외 발생")
        void refresh_tokenNotInRedis_throwsException() {
            // 새 토큰 생성하지만 Redis에 저장 안 함
            String notStoredToken = jwtTokenProvider.createRefreshToken(testUser.getUserId());

            assertThatThrownBy(() -> tokenService.refresh(notStoredToken))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh Token이 일치하지 않습니다.");
        }

        @Test
        @DisplayName("Redis에 저장된 토큰과 불일치 시 예외 발생")
        void refresh_tokenMismatch_throwsException() {
            // 임의의 다른 토큰을 수동으로 Redis에 덮어쓰기
            refreshTokenStore.save(
                    String.valueOf(testUser.getUserId()),
                    "manually-different-token",
                    jwtTokenProvider.getRefreshExpSeconds()
            );

            // 기존 validRefreshToken으로 요청 → Redis 값과 불일치
            assertThatThrownBy(() -> tokenService.refresh(validRefreshToken))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Refresh Token이 일치하지 않습니다.");
        }
    }
}
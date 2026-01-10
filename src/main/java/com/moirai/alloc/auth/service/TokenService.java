package com.moirai.alloc.auth.service;

import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenStore tokenStore;

    public AuthResponse refresh(String refreshToken) {

        // 1. Refresh Token 유효성 검사
        if (!StringUtils.hasText(refreshToken) || !jwtTokenProvider.validate(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }

        // 2. Refresh Token에서 userId 추출
        Claims claims = jwtTokenProvider.getClaims(refreshToken);
        String userId = claims.getSubject();

        // 3. Redis에 저장된 refreshToken과 일치하는지 확인
        String storedToken = tokenStore.get(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        // 4. 사용자 조회
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

        // 5. Access Token 재발급
        String newAccessToken = jwtTokenProvider.createAccessToken(
                userId,
                Map.of(
                        "email", user.getEmail(),
                        "name", user.getUserName(),
                        "role", user.getAuth().name() // Enum → String
                )
        );

        // 6. 응답 생성 (refreshToken은 쿠키로 관리 → 응답에서 제외)
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .isNewUser(false)
                .build();
    }
}

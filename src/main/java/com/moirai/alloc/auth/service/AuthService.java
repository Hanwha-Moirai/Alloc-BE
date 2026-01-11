package com.moirai.alloc.auth.service;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore tokenStore;
    private final RedisTemplate<String, String> redisTemplate;


    public AuthResponse login(LoginRequest request) {

        // 1) loginId로 사용자 조회
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String userId = String.valueOf(user.getUserId());

        // 3) Access Token 생성 (필요한 claims만)
        String accessToken = jwtTokenProvider.createAccessToken(
                userId,
                Map.of(
                        "email", user.getEmail(),
                        "role", user.getAuth().name()
                )
        );

        // 4) Refresh Token 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(Long.valueOf(userId));

        // 5) Refresh Token 저장
        tokenStore.save(userId, refreshToken ,jwtTokenProvider.getRefreshExpSeconds());

        log.info("LOGIN success userId={}, role={}", userId, user.getAuth().name());

        return new AuthResponse(accessToken, false);
    }
}


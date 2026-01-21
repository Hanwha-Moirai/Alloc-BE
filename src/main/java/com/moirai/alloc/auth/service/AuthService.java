package com.moirai.alloc.auth.service;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetConfirmRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetSendRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetVerifyRequest;
import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.auth.dto.response.PasswordResetSendResponse;
import com.moirai.alloc.auth.dto.response.PasswordResetVerifyResponse;
import com.moirai.alloc.auth.policy.PasswordPolicyValidator;
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

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int OTP_TTL_SECONDS = 600;      // 10분
    private static final int COOLDOWN_SECONDS = 90;      // 90초
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore tokenStore;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;

    private final PasswordPolicyValidator passwordPolicyValidator;
    private final SecureRandom secureRandom = new SecureRandom();


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

    public PasswordResetSendResponse sendPasswordResetCode(PasswordResetSendRequest request) {
        String email = normalizeEmail(request.getEmail());

        // 1) 쿨다운 체크
        String cooldownKey = cooldownKey(email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            return new PasswordResetSendResponse(remainingTtlSeconds(cooldownKey), remainingTtlSeconds(codeKey(email)));
        }

        // 2) 사용자 존재 여부 확인
        boolean exists = userRepository.existsByEmail(email);

        // 3) OTP 생성 + 저장 (존재하는 이메일일 때만 저장)
        if (exists) {
            String code = generate6DigitCode();
            String hashed = hashOtp(email, code);

            redisTemplate.opsForValue().set(codeKey(email), hashed, OTP_TTL_SECONDS, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(attemptKey(email), "0", OTP_TTL_SECONDS, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

            // 4) 이메일 발송
            mailService.sendPasswordResetCode(email, code); // 구현체에서 템플릿 처리
            log.info("Password reset code sent. email={}", email);
        } else {
            // 존재하지 않아도 응답은 동일
            log.info("Password reset requested for non-existing email={}", email);
            redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);
        }

        return new PasswordResetSendResponse(
                COOLDOWN_SECONDS,
                OTP_TTL_SECONDS
        );
    }

    public PasswordResetVerifyResponse verifyPasswordResetCode(PasswordResetVerifyRequest request) {
        String email = normalizeEmail(request.getEmail());
        String inputCode = request.getCode();

        String stored = redisTemplate.opsForValue().get(codeKey(email));
        if (stored == null) {
            throw new IllegalArgumentException("인증 코드가 만료되었거나 존재하지 않습니다.");
        }

        // 시도 횟수 체크
        int attempts = incrementAttempts(email);
        if (attempts > MAX_ATTEMPTS) {
            // 코드도 폐기해버리면 재시도 유도 가능
            redisTemplate.delete(codeKey(email));
            throw new IllegalArgumentException("인증 시도 횟수를 초과했습니다. 다시 요청해주세요.");
        }

        // 코드 검증
        String expected = hashOtp(email, inputCode);
        if (!stored.equals(expected)) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        }

        // 성공: resetToken 발급 (1회용)
        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(tokenKey(resetToken), email, OTP_TTL_SECONDS, TimeUnit.SECONDS);

        // code/attempt 폐기(재사용 방지)
        redisTemplate.delete(codeKey(email));
        redisTemplate.delete(attemptKey(email));

        return new PasswordResetVerifyResponse(resetToken, OTP_TTL_SECONDS);
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request) {

        String newPw = request.getNewPassword();
        String newPwConfirm = request.getNewPasswordConfirm();

        if (newPw == null || newPwConfirm == null) {
            throw new IllegalArgumentException("새 비밀번호는 필수입니다.");
        }
        if (!newPw.equals(newPwConfirm)) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        String resetToken = request.getResetToken();
        if (resetToken == null || resetToken.isBlank()) {
            throw new IllegalArgumentException("resetToken은 필수입니다.");
        }

        String email = redisTemplate.opsForValue().get(tokenKey(resetToken));
        if (email == null) {
            throw new IllegalArgumentException("비밀번호 재설정 토큰이 만료되었거나 유효하지 않습니다.");
        }

        // 토큰 1회용 처리: 먼저 삭제(중복 사용 방지)
        redisTemplate.delete(tokenKey(resetToken));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 정책 검증(평문 기준)
        passwordPolicyValidator.validate(newPw);

        // 암호화 후 저장
        user.changePassword(passwordEncoder.encode(newPw));
        userRepository.save(user);

        // 기존 refresh 토큰 무효화
        tokenStore.delete(String.valueOf(user.getUserId()));

        log.info("Password reset success. userId={}, email={}", user.getUserId(), email);
    }

    private String normalizeEmail(String email) {
        if (email == null) throw new IllegalArgumentException("email은 필수입니다.");
        return email.trim().toLowerCase();
    }

    private String generate6DigitCode() {
        int n = secureRandom.nextInt(1_000_000);
        return String.format("%06d", n);
    }

    // 추후 HMAC/SHA-256로 변경예정
    private String hashOtp(String email, String code) {
        return Integer.toHexString((email + ":" + code).hashCode());
    }

    private int incrementAttempts(String email) {
        String key = attemptKey(email);
        String cur = redisTemplate.opsForValue().get(key);
        int next = (cur == null) ? 1 : (Integer.parseInt(cur) + 1);
        redisTemplate.opsForValue().set(key, String.valueOf(next), OTP_TTL_SECONDS, TimeUnit.SECONDS);
        return next;
    }

    private long remainingTtlSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (ttl == null || ttl < 0) ? 0 : ttl;
    }

    private String codeKey(String email) { return "pwdreset:code:" + email; }
    private String attemptKey(String email) { return "pwdreset:attempt:" + email; }
    private String tokenKey(String token) { return "pwdreset:token:" + token; }
    private String cooldownKey(String email) { return "pwdreset:cooldown:" + email; }
}


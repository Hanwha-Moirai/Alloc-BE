package com.moirai.alloc.auth.controller;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetConfirmRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetSendRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetVerifyRequest;
import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.auth.dto.response.AuthTokens;
import com.moirai.alloc.auth.dto.response.PasswordResetSendResponse;
import com.moirai.alloc.auth.dto.response.PasswordResetVerifyResponse;
import com.moirai.alloc.auth.service.AuthService;
import com.moirai.alloc.auth.service.TokenService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final RefreshTokenStore tokenStore;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${auth.cookie.access-name:accessToken}")
    private String accessCookieName;

    @Value("${auth.cookie.refresh-name:refreshToken}")
    private String refreshCookieName;

    @Value("${auth.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${auth.cookie.secure:false}")
    private boolean cookieSecure;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);

        ResponseCookie accessCookie = buildAccessCookie(tokens.accessToken());
        ResponseCookie refreshCookie = buildRefreshCookie(tokens.refreshToken());

        AuthResponse response = new AuthResponse(tokens.accessToken(), tokens.isNewUser());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "${auth.cookie.refresh-name:refreshToken}", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        AuthResponse response = tokenService.refresh(refreshToken);
        ResponseCookie accessCookie = buildAccessCookie(response.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @CookieValue(name = "${auth.cookie.refresh-name:refreshToken}", required = false) String refreshToken) {

        // 1) principal로 삭제
        if (principal != null) {
            tokenStore.delete(String.valueOf(principal.userId()));
        }
        // 2) principal 없으면 쿠키의 refreshToken에서 userId 추출해서 삭제
        else if (refreshToken != null && jwtTokenProvider.validate(refreshToken)) {
            String userId = jwtTokenProvider.getClaims(refreshToken).getSubject();
            tokenStore.delete(userId);
        }

        // 쿠키 삭제 (항상 수행)
        ResponseCookie deleteRefreshCookie = ResponseCookie.from(refreshCookieName, "")
                .maxAge(0)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure)
                .httpOnly(true)
                .build();

        ResponseCookie deleteAccessCookie = ResponseCookie.from(accessCookieName, "")
                .maxAge(0)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure)
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString())
                .body(ApiResponse.success(null));
    }

    // 1) 비밀번호 재설정 - 인증코드 발송
    @PostMapping("/password/reset/send")
    public ResponseEntity<ApiResponse<PasswordResetSendResponse>> sendResetCode(
            @RequestBody PasswordResetSendRequest request
    ) {
        PasswordResetSendResponse response = authService.sendPasswordResetCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 2) 비밀번호 재설정 - 인증코드 검증 (성공 시 resetToken 발급)
    @PostMapping("/password/reset/verify")
    public ResponseEntity<ApiResponse<PasswordResetVerifyResponse>> verifyResetCode(
            @RequestBody PasswordResetVerifyRequest request
    ) {
        PasswordResetVerifyResponse response = authService.verifyPasswordResetCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 3) 비밀번호 재설정 - 새 비밀번호 확정
    @PostMapping("/password/reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmResetPassword(
            @RequestBody PasswordResetConfirmRequest request
    ) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private ResponseCookie buildAccessCookie(String accessToken) {
        return ResponseCookie.from(accessCookieName, accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(jwtTokenProvider.getAccessExpSeconds())
                .build();
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(jwtTokenProvider.getRefreshExpSeconds())
                .build();
    }
}

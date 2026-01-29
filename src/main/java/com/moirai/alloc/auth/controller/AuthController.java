package com.moirai.alloc.auth.controller;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetConfirmRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetSendRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetVerifyRequest;
import com.moirai.alloc.auth.cookie.AuthCookieService;
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
import org.springframework.http.HttpHeaders;
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
    private final AuthCookieService authCookieService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokens.accessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokens.refreshToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.createCsrfTokenCookie(authCookieService.newCsrfToken()).toString());

        return ResponseEntity.ok().headers(headers).body(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        AuthTokens tokens = tokenService.refresh(refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.createAccessTokenCookie(tokens.accessToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.createRefreshTokenCookie(tokens.refreshToken()).toString());
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.createCsrfTokenCookie(authCookieService.newCsrfToken()).toString());

        return ResponseEntity.ok().headers(headers).body(ApiResponse.success(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

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
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.deleteAccessTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.deleteRefreshTokenCookie().toString());
        headers.add(HttpHeaders.SET_COOKIE, authCookieService.deleteCsrfTokenCookie().toString());

        return ResponseEntity.ok().headers(headers).body(ApiResponse.success(null));
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
}

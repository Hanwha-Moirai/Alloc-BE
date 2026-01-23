package com.moirai.alloc.auth.controller;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.auth.service.AuthService;
import com.moirai.alloc.auth.service.TokenService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final RefreshTokenStore tokenStore;
    private final JwtTokenProvider jwtTokenProvider;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        AuthResponse response = tokenService.refresh(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response));
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
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .path("/")
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.success(null));
    }
}

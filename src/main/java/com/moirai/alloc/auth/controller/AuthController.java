package com.moirai.alloc.auth.controller;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetConfirmRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetSendRequest;
import com.moirai.alloc.auth.dto.request.PasswordResetVerifyRequest;
import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.auth.dto.response.PasswordResetSendResponse;
import com.moirai.alloc.auth.dto.response.PasswordResetVerifyResponse;
import com.moirai.alloc.auth.service.AuthService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenStore tokenStore;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal) {
        tokenStore.delete(String.valueOf(principal.userId()));
        return ResponseEntity.ok(ApiResponse.success(null));
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

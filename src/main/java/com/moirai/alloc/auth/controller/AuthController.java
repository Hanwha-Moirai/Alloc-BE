package com.moirai.alloc.auth.controller;

import com.moirai.alloc.auth.dto.request.LoginRequest;
import com.moirai.alloc.auth.dto.response.AuthResponse;
import com.moirai.alloc.auth.service.AuthService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.RefreshTokenStore;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
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
}

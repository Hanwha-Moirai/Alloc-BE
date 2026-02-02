package com.moirai.alloc.user.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.user.command.dto.response.UserMeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserMeController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserMeResponse>> me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 없습니다.");
        }

        UserMeResponse response = new UserMeResponse(
                principal.userId(),
                principal.loginId(),
                principal.email(),
                principal.name(),
                principal.role()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

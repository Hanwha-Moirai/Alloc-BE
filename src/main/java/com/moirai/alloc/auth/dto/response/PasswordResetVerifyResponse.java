package com.moirai.alloc.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetVerifyResponse {
    private String resetToken;
    private int expiresInSeconds;
}
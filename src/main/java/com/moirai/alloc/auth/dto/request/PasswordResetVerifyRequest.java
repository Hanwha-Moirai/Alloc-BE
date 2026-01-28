package com.moirai.alloc.auth.dto.request;

import lombok.Getter;

@Getter
public class PasswordResetVerifyRequest {
    private String email;
    private String code; // 6자리
}

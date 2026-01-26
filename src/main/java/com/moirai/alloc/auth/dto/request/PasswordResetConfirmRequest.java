package com.moirai.alloc.auth.dto.request;

import lombok.Getter;

@Getter
public class PasswordResetConfirmRequest {
    private String resetToken;
    private String newPassword;
    private String newPasswordConfirm;
}
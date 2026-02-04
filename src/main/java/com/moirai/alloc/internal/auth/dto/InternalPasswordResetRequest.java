package com.moirai.alloc.internal.auth.dto;

public record InternalPasswordResetRequest(
        String email,
        String newPassword
) {
}

package com.moirai.alloc.auth.dto.response;

public record AuthResponse(
        String accessToken,
        boolean isNewUser
) {
}
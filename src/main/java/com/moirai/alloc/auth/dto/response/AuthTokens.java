package com.moirai.alloc.auth.dto.response;

public record AuthTokens(
        String accessToken,
        String refreshToken
) {
}

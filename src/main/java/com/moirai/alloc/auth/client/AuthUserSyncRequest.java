package com.moirai.alloc.auth.client;

public record AuthUserSyncRequest(
        Long userId,
        String loginId,
        String email,
        String password,
        String role,
        String status
) {
}

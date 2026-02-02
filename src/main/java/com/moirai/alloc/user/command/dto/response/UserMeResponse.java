package com.moirai.alloc.user.command.dto.response;

public record UserMeResponse(
        Long userId,
        String loginId,
        String email,
        String name,
        String role
) {
}

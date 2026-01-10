package com.moirai.alloc.auth.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private boolean isNewUser;
}


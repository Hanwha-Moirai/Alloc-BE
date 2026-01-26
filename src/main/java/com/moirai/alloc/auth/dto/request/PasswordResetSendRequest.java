package com.moirai.alloc.auth.dto.request;

import lombok.Getter;

@Getter
public class PasswordResetSendRequest {
    private String email;
}
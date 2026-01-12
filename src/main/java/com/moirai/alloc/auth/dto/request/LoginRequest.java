package com.moirai.alloc.auth.dto.request;

import lombok.Getter;

@Getter
public class LoginRequest {

    private String loginId;
    private String password;
}

package com.moirai.alloc.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetSendResponse {
    private long cooldownSeconds;  // 이메일 재전송 가능까지 남은 시간
    private long expiresInSeconds; //발송된 인증 코드 만료 시간
}

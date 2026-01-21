package com.moirai.alloc.auth.policy;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }
        if (rawPassword.length() < 10) {
            throw new IllegalArgumentException("비밀번호는 최소 10자리 이상이어야 합니다.");
        }

        boolean hasLetter = rawPassword.matches(".*[A-Za-z].*");
        boolean hasDigit = rawPassword.matches(".*\\d.*");
        boolean hasSpecial = rawPassword.matches(".*[^A-Za-z0-9].*");

        int types = 0;
        if (hasLetter) types++;
        if (hasDigit) types++;
        if (hasSpecial) types++;

        if (types < 2) {
            throw new IllegalArgumentException("영문, 숫자, 특수문자 중 2종류 이상을 포함해야 합니다.");
        }
    }
}

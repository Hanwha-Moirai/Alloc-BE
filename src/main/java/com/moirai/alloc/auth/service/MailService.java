package com.moirai.alloc.auth.service;

public interface MailService {
    void sendPasswordResetCode(String toEmail, String code);
}

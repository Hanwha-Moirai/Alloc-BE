package com.moirai.alloc.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:no-reply@alloc.com}")
    private String from;

    @Value("${mail.appName:ALLOC}")
    private String appName;

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "[" + appName + "] 비밀번호 재설정 인증 코드";
        String html = buildPasswordResetHtml(code);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML

            mailSender.send(message);

            log.info("Password reset email sent. to={}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to build email. to={}", toEmail, e);
            throw new IllegalStateException("메일 메시지 생성에 실패했습니다.");
        } catch (MailException e) {
            log.error("Failed to send email. to={}", toEmail, e);
            throw new IllegalStateException("메일 전송에 실패했습니다.");
        }
    }

    private String buildPasswordResetHtml(String code) {
        // MVP: 템플릿 엔진 없이 문자열로 구성
        // (추후 Thymeleaf/FreeMarker로 바꿔도 됨)
        return """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                  <h2>비밀번호 재설정 인증 코드</h2>
                  <p>아래 인증 코드를 화면에 입력해주세요.</p>
                  <div style="padding: 12px 16px; background: #f4f6f8; display: inline-block; border-radius: 8px; font-size: 20px; letter-spacing: 2px;">
                    <b>%s</b>
                  </div>
                  <p style="margin-top: 16px; color: #666;">
                    이 코드는 일정 시간이 지나면 만료됩니다.
                  </p>
                </div>
                """.formatted(code);
    }
}

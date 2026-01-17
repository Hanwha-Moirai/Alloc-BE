package com.moirai.alloc.notification.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "alarm_send_log",
        indexes = {
                @Index(name = "idx_send_log_template", columnList = "template_id"),
                @Index(name = "idx_send_log_user", columnList = "user_id"),
                @Index(name = "idx_send_log_status", columnList = "log_status"),
                @Index(name = "idx_send_log_sent_at", columnList = "sent_at")
        }
)
public class AlarmSendLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_log_id")
    private Long id;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_status", nullable = false, length = 20)
    private SendLogStatus logStatus;

    /** 감사/장애 분석용: 당시 발송 바디(스냅샷) */
    @Column(name = "template_context", nullable = false, length = 255)
    private String templateContext;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private AlarmSendLog(Long templateId,
                         Long userId,
                         SendLogStatus logStatus,
                         String templateContext,
                         LocalDateTime sentAt) {
        this.templateId = templateId;
        this.userId = userId;
        this.templateContext = templateContext;
        this.sentAt = (sentAt != null) ? sentAt : LocalDateTime.now();
        this.logStatus = (logStatus != null) ? logStatus : SendLogStatus.PENDING;
    }

    public void markSuccess() { this.logStatus = SendLogStatus.SUCCESS; }
    public void markFailed() { this.logStatus = SendLogStatus.FAILED; }
}

package com.moirai.alloc.notification.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "alarm_send_log",
        indexes = {
                @Index(name = "idx_send_log_template", columnList = "template_id"),
                @Index(name = "idx_send_log_user", columnList = "user_id"),
                @Index(name = "idx_send_log_status", columnList = "log_status")
        }
)
public class AlarmSendLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_log_id")
    private Long id;

    /** FK: alarm_template.template_id */
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    /** FK: users.user_id */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_status", nullable = false)
    private SendLogStatus logStatus = SendLogStatus.PENDING;

    @Column(name = "template_context", nullable = false, length = 255)
    private String templateContext;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}

package com.moirai.alloc.notification.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "alarm_log",
        indexes = {
                @Index(name = "idx_alarm_user_read_deleted", columnList = "user_id,is_read,is_deleted"),
                @Index(name = "idx_alarm_created", columnList = "created_at"),
                @Index(name = "idx_alarm_template", columnList = "template_id")
        }
)
public class AlarmLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FK: alarm_template.template_id */
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "alarm_title", nullable = false, length = 50)
    private String alarmTitle;

    @Column(name = "alarm_context", nullable = false, length = 255)
    private String alarmContext;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Builder
    private AlarmLog(
            Long userId,
            Long templateId,
            String alarmTitle,
            String alarmContext,
            TargetType targetType,
            String linkUrl,
            Long targetId
    ) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.templateId = Objects.requireNonNull(templateId, "templateId");
        this.alarmTitle = Objects.requireNonNull(alarmTitle, "alarmTitle");
        this.alarmContext = Objects.requireNonNull(alarmContext, "alarmContext");

        this.targetType = Objects.requireNonNull(targetType, "targetType");
        this.targetId = Objects.requireNonNull(targetId, "targetId");
        this.linkUrl = linkUrl;

        this.read = false;
        this.deleted = false;
    }


    public void markRead() {
        this.read = true;
    }

    public void softDelete() {
        this.deleted = true;
    }
}

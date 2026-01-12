package com.moirai.alloc.notification.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "alarm_log",
        indexes = {
                @Index(name = "idx_alarm_user_read", columnList = "user_id,is_read"),
                @Index(name = "idx_alarm_created", columnList = "created_at"),
                @Index(name = "idx_alarm_template", columnList = "template_id")
        }
)
public class AlarmLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    /** FK: users.user_id (수신자) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FK: alarm_template.template_id */
    @Column(name = "template_id", nullable = false)
    private Long templateId;

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
            Boolean read,
            TargetType targetType,
            String linkUrl,
            Long targetId,
            Boolean deleted
    ) {
        this.userId = userId;
        this.templateId = templateId;
        this.linkUrl = linkUrl;
        this.targetId = targetId;

        this.read = (read != null) ? read : false;
        this.deleted = (deleted != null) ? deleted : false;
        this.targetType = (targetType != null) ? targetType : TargetType.POST;
    }

    public void markRead() {
        this.read = true;
    }

    public void softDelete() {
        this.deleted = true;
    }
}

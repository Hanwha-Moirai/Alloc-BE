package com.moirai.alloc.notification.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "alarm_log",
        indexes = {
                @Index(name = "idx_alarm_user_read", columnList = "user_id,is_read"),
                @Index(name = "idx_alarm_created", columnList = "created_at")
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

    @Column(name = "alarm_title", nullable = false, length = 150)
    private String alarmTitle;

    @Column(name = "alarm_context", nullable = false, length = 255)
    private String alarmContext;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_type", nullable = false)
    private AlarmType alarmType = AlarmType.POST;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType = TargetType.POST;

    @Column(name = "link_url", length = 255)
    private String linkUrl;

    @Column(name = "target_id", nullable = false)
    private Long targetId;
}

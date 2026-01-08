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
        name = "alarm_template",
        indexes = {
                @Index(name = "idx_alarm_template_user", columnList = "user_id"),
                @Index(name = "idx_alarm_template_deleted", columnList = "is_deleted")
        }
)
public class AlarmTemplate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long id;

    @Column(name = "template_title", nullable = false, length = 50)
    private String templateTitle;

    @Column(name = "template_context", nullable = false, length = 255)
    private String templateContext;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    /** FK: users.user_id (템플릿 생성자) */
    @Column(name = "user_id", nullable = false)
    private Long userId;
}

package com.moirai.alloc.notification.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    /** FK: users.user_id (템플릿 생성자) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_title", nullable = false, length = 50)
    private String templateTitle;

    @Column(name = "template_context", nullable = false, length = 255)
    private String templateContext;

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_template_type", nullable = false)
    private AlarmTemplateType alarmTemplateType;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Builder
    private AlarmTemplate(
            Long userId,
            String templateTitle,
            String templateContext,
            AlarmTemplateType alarmTemplateType,
            Boolean deleted
    ) {
        this.userId = userId;
        this.templateTitle = templateTitle;
        this.templateContext = templateContext;

        this.deleted = (deleted != null) ? deleted : false;
        this.alarmTemplateType = (alarmTemplateType != null) ? alarmTemplateType : AlarmTemplateType.POST_TEMP;
    }

    public void softDelete() {
        this.deleted = true;
    }
}

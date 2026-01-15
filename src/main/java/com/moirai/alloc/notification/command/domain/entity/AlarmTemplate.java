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
                @Index(name = "idx_alarm_template_deleted", columnList = "is_deleted"),
                @Index(name = "idx_alarm_template_type", columnList = "alarm_template_type")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_template_type", nullable = false)
    private AlarmTemplateType alarmTemplateType;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Builder
    private AlarmTemplate(String templateTitle,
                          String templateContext,
                          AlarmTemplateType alarmTemplateType) {
        this.templateTitle = templateTitle;
        this.templateContext = templateContext;
        this.alarmTemplateType = alarmTemplateType;
        this.deleted = false;
    }

    public void softDelete() { this.deleted = true; }
    public void restore() { this.deleted = false; }

    public void update(String title, String context, AlarmTemplateType type) {
        if (title != null && !title.isBlank()) this.templateTitle = title;
        if (context != null && !context.isBlank()) this.templateContext = context;
        if (type != null) this.alarmTemplateType = type;
    }
}

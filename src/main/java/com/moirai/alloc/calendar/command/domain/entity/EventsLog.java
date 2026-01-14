package com.moirai.alloc.calendar.command.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "events_log",
        indexes = {
                @Index(name = "idx_event_log_event", columnList = "event_id"),
                @Index(name = "idx_event_log_actor", columnList = "actor_user_id"),
                @Index(name = "idx_event_log_created", columnList = "created_at")
        }
)
public class EventsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_log_id")
    private Long id;

    /** FK: events.event_id */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** FK: employee.user_id (누가 변경했는지) */
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Lob
    @Column(name = "log_description")
    private String logDescription;

    @Column(name = "before_start_date")
    private LocalDateTime beforeStartDate;

    @Column(name = "after_start_date")
    private LocalDateTime afterStartDate;

    @Column(name = "before_end_date")
    private LocalDateTime beforeEndDate;

    @Column(name = "after_end_date")
    private LocalDateTime afterEndDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private EventsLog(
            Long eventId,
            Long actorUserId,
            ChangeType changeType,
            String logDescription,
            LocalDateTime beforeStartDate,
            LocalDateTime afterStartDate,
            LocalDateTime beforeEndDate,
            LocalDateTime afterEndDate
    ) {
        this.eventId = eventId;
        this.actorUserId = actorUserId;
        this.logDescription = logDescription;
        this.beforeStartDate = beforeStartDate;
        this.afterStartDate = afterStartDate;
        this.beforeEndDate = beforeEndDate;
        this.afterEndDate = afterEndDate;

        this.changeType = (changeType == null) ? ChangeType.CREATE : changeType;
    }
}
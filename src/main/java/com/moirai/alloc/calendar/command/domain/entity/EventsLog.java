package com.moirai.alloc.calendar.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "event_log",
        indexes = {
                @Index(name = "idx_event_log_event", columnList = "event_id"),
                @Index(name = "idx_event_log_actor", columnList = "actor_user_id"),
                @Index(name = "idx_event_log_created", columnList = "created_at")
        }
)
public class EventsLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_log_id")
    private Long id;

    /** FK: event.event_id */
    @Column(name = "event_id", nullable = false)
    private Long eventId;

    /** FK: employee.user_id (누가 변경했는지) */
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType = ChangeType.CREATE;

    @Lob
    @Column(name = "log_description")
    private String logDescription;

    @Column(name = "before_date", nullable = false)
    private LocalDateTime beforeDate;

    @Column(name = "after_date", nullable = false)
    private LocalDateTime afterDate;

}

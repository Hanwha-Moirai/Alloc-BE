package com.moirai.alloc.calendar.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(
        name = "events",
        indexes = {
                @Index(name = "idx_event_project", columnList = "project_id"),
                @Index(name = "idx_event_owner", columnList = "user_id"),
                @Index(name = "idx_event_period", columnList = "start_date,end_date")
        }
)
public class Events extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    /** FK: project.project_id */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /** FK: employee.user_id (소유자/작성자) */
    @Column(name = "user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "event_name", nullable = false, length = 150)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_state", nullable = false)
    private EventState eventState = EventState.IN_PROGRESS;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType = EventType.PRIVATE;

    @Column(name = "event_place", length = 150)
    private String eventPlace;

    @Lob
    @Column(name = "event_description", nullable = false)
    private String eventDescription;
}

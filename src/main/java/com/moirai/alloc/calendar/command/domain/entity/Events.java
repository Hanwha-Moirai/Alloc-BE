package com.moirai.alloc.calendar.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private EventState eventState;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;

    @Column(name = "event_place", length = 150)
    private String eventPlace;

    @Lob
    @Column(name = "event_description", nullable = false)
    private String eventDescription;

    /** DDL: is_deleted BOOLEAN NOT NULL DEFAULT FALSE */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Builder
    private Events(
            Long projectId,
            Long ownerUserId,
            String eventName,
            EventState eventState,
            LocalDateTime startDate,
            LocalDateTime endDate,
            EventType eventType,
            String eventPlace,
            String eventDescription,
            Boolean deleted
    ) {
        this.projectId = projectId;
        this.ownerUserId = ownerUserId;
        this.eventName = eventName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventPlace = eventPlace;
        this.eventDescription = eventDescription;

        this.eventState = (eventState == null) ? EventState.IN_PROGRESS : eventState;
        this.eventType = (eventType == null) ? EventType.PRIVATE : eventType;
        this.deleted = (deleted != null) ? deleted : false;
    }

    public void softDelete() {
        this.deleted = true;
    }

    public void updateEventState(EventState eventState) {
        this.eventState = eventState;
    }

    public void updateEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public void updateEventName(String eventName) {
        this.eventName = eventName;
    }

    public void updateEventPlace(String eventPlace) {
        this.eventPlace = eventPlace;
    }

    public void updateEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void updatePeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

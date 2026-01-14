package com.moirai.alloc.calendar.command.dto.response;

import com.moirai.alloc.calendar.command.domain.entity.EventState;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.domain.entity.Events;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {
    private Long eventId;
    private Long projectId;
    private Long ownerUserId;
    private String eventName;
    private EventType eventType;
    private EventState eventState;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String place;
    private String description;

    public static EventResponse from(Events event) {
        return EventResponse.builder()
                .eventId(event.getId())
                .projectId(event.getProjectId())
                .ownerUserId(event.getOwnerUserId())
                .eventName(event.getEventName())
                .eventType(event.getEventType())
                .eventState(event.getEventState())
                .startDateTime(event.getStartDate())
                .endDateTime(event.getEndDate())
                .place(event.getEventPlace())
                .description(event.getEventDescription())
                .build();
    }
}

package com.moirai.alloc.calendar.command.dto.response;

import com.moirai.alloc.calendar.command.domain.entity.EventState;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.domain.entity.Events;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EventDetailResponse {
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

    private List<Long> memberUserIds;

    /** 이름 출력용 */
    private List<EventMemberResponse> members;

    public static EventDetailResponse from(Events event, List<Long> memberUserIds, List<EventMemberResponse> members) {
        return EventDetailResponse.builder()
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
                .memberUserIds(memberUserIds)
                .members(members)
                .build();
    }
}

package com.moirai.alloc.calendar.command.dto.request;

import com.moirai.alloc.calendar.command.domain.entity.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EventUpdateRequest {
    private String eventName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private EventType eventType;
    private String place;
    private String description;
    private List<Long> memberUserIds;
}

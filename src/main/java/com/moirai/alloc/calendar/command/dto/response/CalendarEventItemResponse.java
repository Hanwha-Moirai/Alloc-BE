package com.moirai.alloc.calendar.command.dto.response;

import com.moirai.alloc.calendar.command.domain.entity.EventState;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalendarEventItemResponse {
    private Long eventId;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventType eventType;
    private EventState eventState;
}

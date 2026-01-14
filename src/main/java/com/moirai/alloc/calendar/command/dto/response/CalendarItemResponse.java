package com.moirai.alloc.calendar.command.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class CalendarItemResponse {
    private CalendarItemType itemType;
    private Long id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String colorHint;
    private Map<String, Object> meta;
}

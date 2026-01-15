package com.moirai.alloc.calendar.command.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CalendarViewResponse {
    private List<CalendarEventItemResponse> items;
}


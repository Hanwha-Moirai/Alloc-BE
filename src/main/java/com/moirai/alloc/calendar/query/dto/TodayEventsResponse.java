package com.moirai.alloc.calendar.query.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TodayEventsResponse(
        LocalDate date,
        List<TodayEventItemResponse> items,
        LocalDateTime nextCursorStart,
        Long nextCursorId
) {}

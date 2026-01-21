package com.moirai.alloc.calendar.query.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectUpcomingEventsResponse(
        Long projectId,
        List<ProjectUpcomingEventItemResponse> items,
        LocalDateTime nextCursorStart,
        Long nextCursorId
) {}

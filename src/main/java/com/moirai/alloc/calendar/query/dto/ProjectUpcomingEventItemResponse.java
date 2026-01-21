package com.moirai.alloc.calendar.query.dto;

import com.moirai.alloc.calendar.command.domain.entity.EventType;
import java.time.LocalDateTime;

public record ProjectUpcomingEventItemResponse(
        Long eventId,
        EventType eventType,
        String label,          // "공유 일정" | "개인 일정" | "휴가"
        String title,
        LocalDateTime start,
        LocalDateTime end,
        long dDay              // start 기준 D-day
) {}

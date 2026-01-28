package com.moirai.alloc.calendar.query.dto;

import com.moirai.alloc.calendar.command.domain.entity.EventType;

import java.time.LocalDateTime;

public record TodayEventItemResponse(
        Long eventId,
        Long projectId,
        String title,
        LocalDateTime start,
        LocalDateTime end,
        EventType eventType
) {}

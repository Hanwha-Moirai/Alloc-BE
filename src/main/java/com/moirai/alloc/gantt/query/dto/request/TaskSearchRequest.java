package com.moirai.alloc.gantt.query.dto.request;

import com.moirai.alloc.gantt.command.domain.entity.Task.TaskStatus;

import java.time.LocalDate;

public record TaskSearchRequest(
        String assigneeName,
        TaskStatus status,
        LocalDate startDate,
        LocalDate endDate
) {
}

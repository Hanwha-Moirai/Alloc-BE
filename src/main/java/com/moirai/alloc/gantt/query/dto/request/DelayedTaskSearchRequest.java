package com.moirai.alloc.gantt.query.dto.request;

public record DelayedTaskSearchRequest(
        Long projectId,
        java.time.LocalDate from,
        java.time.LocalDate to,
        String taskName,
        String projectName,
        String assigneeName,
        Integer delayedDays
) {
}

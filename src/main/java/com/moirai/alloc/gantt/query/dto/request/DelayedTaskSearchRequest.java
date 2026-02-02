package com.moirai.alloc.gantt.query.dto.request;

public record DelayedTaskSearchRequest(
        String taskName,
        String projectName,
        String assigneeName,
        Integer delayedDays
) {
}

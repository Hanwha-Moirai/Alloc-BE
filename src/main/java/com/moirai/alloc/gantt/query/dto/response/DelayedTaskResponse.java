package com.moirai.alloc.gantt.query.dto.response;

public record DelayedTaskResponse(
        String taskName,
        String projectName,
        String assigneeName,
        Integer delayedDays
) {
}

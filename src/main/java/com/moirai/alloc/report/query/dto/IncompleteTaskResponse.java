package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.gantt.command.domain.entity.Task;

public record IncompleteTaskResponse(
        Long taskId,
        String taskName,
        String assigneeName,
        Task.TaskCategory taskCategory,
        Integer delayedDates,
        String delayReason
) {
}

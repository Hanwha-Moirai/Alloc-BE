package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.gantt.command.domain.entity.Task;

import java.time.LocalDateTime;

public record CompletedTaskResponse(
        Long taskId,
        String taskName,
        String assigneeName,
        Task.TaskCategory taskCategory,
        LocalDateTime completionDate
) {
}

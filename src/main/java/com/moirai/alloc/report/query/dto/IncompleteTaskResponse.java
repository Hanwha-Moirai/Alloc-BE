package com.moirai.alloc.report.query.dto;

import com.moirai.alloc.gantt.command.domain.entity.Task;

import java.time.LocalDate;

public record IncompleteTaskResponse(
        Long taskId,
        String taskName,
        String assigneeName,
        Task.TaskCategory taskCategory,
        LocalDate dueDate,
        String delayReason
) {
}

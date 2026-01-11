package com.moirai.alloc.gantt.command.application.dto.request;

import com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory;

import java.time.LocalDate;

public record CreateTaskRequest(
        Long milestoneId,
        Long assigneeId,
        TaskCategory taskCategory,
        String taskName,
        String taskDescription,
        LocalDate startDate,
        LocalDate endDate
) {
}

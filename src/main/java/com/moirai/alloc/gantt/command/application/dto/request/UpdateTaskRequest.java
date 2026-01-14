package com.moirai.alloc.gantt.command.application.dto.request;

import com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory;
import com.moirai.alloc.gantt.command.domain.entity.Task.TaskStatus;

import java.time.LocalDate;

public record UpdateTaskRequest(
        Long milestoneId,
        Long assigneeId,
        TaskCategory taskCategory,
        String taskName,
        String taskDescription,
        TaskStatus taskStatus,
        LocalDate startDate,
        LocalDate endDate
) {
}

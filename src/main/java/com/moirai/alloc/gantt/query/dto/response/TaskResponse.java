package com.moirai.alloc.gantt.query.dto.response;

import com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory;
import com.moirai.alloc.gantt.command.domain.entity.Task.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long taskId,
        Long milestoneId,
        //Long userId,
        String userName,
        TaskCategory taskCategory,
        String taskName,
        String taskDescription,
        TaskStatus taskStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isCompleted,
        Boolean isDeleted
) {
}

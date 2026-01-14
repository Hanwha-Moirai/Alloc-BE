package com.moirai.alloc.calendar.query.dto;

import com.moirai.alloc.gantt.command.domain.entity.Task;

import java.time.LocalDate;

public record TaskCalendarRow(
        Long taskId,
        String taskName,
        LocalDate startDate,
        LocalDate endDate,
        Task.TaskStatus taskStatus,
        Task.TaskCategory taskCategory,
        Long assigneeUserId,
        Long milestoneId
) {}

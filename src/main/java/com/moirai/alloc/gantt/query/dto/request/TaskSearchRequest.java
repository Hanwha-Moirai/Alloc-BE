package com.moirai.alloc.gantt.query.dto.request;

import com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory;
import com.moirai.alloc.gantt.command.domain.entity.Task.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public record TaskSearchRequest(
        TaskStatus status,
        LocalDate startDate,
        LocalDate endDate,
        List<TaskCategory> taskCategories,
        List<String> assigneeNames,
        List<String> periods
) {
}

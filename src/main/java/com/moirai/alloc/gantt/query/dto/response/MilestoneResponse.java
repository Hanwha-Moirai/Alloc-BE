package com.moirai.alloc.gantt.query.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MilestoneResponse(
        Long milestoneId,
        Long projectId,
        String milestoneName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDate startDate,
        LocalDate endDate,
        Long achievementRate,
        Boolean isDeleted,
        List<TaskResponse> tasks
) {
}

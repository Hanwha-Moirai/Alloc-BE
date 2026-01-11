package com.moirai.alloc.gantt.query.dto.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MilestoneProjection(
        Long milestoneId,
        Long projectId,
        String milestoneName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDate startDate,
        LocalDate endDate,
        Long achievementRate,
        Boolean isDeleted
) {
}

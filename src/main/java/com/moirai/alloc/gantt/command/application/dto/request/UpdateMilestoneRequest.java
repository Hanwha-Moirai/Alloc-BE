package com.moirai.alloc.gantt.command.application.dto.request;

import java.time.LocalDate;

public record UpdateMilestoneRequest(
        String milestoneName,
        LocalDate startDate,
        LocalDate endDate,
        Long achievementRate
) {
}

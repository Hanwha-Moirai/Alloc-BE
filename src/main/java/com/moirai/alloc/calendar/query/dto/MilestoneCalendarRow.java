package com.moirai.alloc.calendar.query.dto;

import java.time.LocalDate;

public record MilestoneCalendarRow(
        Long milestoneId,
        String milestoneName,
        LocalDate startDate,
        LocalDate endDate,
        Long achievementRate
) {}

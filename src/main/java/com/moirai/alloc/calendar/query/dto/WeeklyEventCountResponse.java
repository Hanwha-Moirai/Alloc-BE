package com.moirai.alloc.calendar.query.dto;

import java.time.LocalDate;

public record WeeklyEventCountResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        long total
) {}

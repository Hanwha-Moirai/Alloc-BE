package com.moirai.alloc.report.command.dto;

import java.time.LocalDate;

public record NextWeekTaskRequest(
        Long taskId,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate
) {
}

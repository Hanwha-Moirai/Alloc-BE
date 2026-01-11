package com.moirai.alloc.report.command.dto;

import java.time.LocalDateTime;

public record WeeklyReportSaveResponse(
        Long reportId,
        LocalDateTime updatedAt
) {
}

package com.moirai.alloc.report.command.dto.response;

import java.time.LocalDateTime;

public record WeeklyReportSaveResponse(
        Long reportId,
        LocalDateTime updatedAt
) {
}

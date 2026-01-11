package com.moirai.alloc.report.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import com.moirai.alloc.report.query.service.WeeklyReportQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/mydocs/report")
public class WeeklyReportMyDocsController {

    private final WeeklyReportQueryService weeklyReportQueryService;

    public WeeklyReportMyDocsController(WeeklyReportQueryService weeklyReportQueryService) {
        this.weeklyReportQueryService = weeklyReportQueryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> getMyReports(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.getMyDocsReports(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> searchMyReports(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) WeeklyReport.ReportStatus reportStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartTo,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        WeeklyReportSearchCondition condition = new WeeklyReportSearchCondition(
                projectId,
                null,
                reportStatus,
                weekStartFrom,
                weekStartTo,
                keyword
        );
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.searchMyDocsReports(principal, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<WeeklyReportDetailResponse>> getMyReportDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long reportId
    ) {
        WeeklyReportDetailResponse response =
                weeklyReportQueryService.getMyDocsReportDetail(principal, reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.moirai.alloc.report.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportMissingResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import com.moirai.alloc.report.query.service.WeeklyReportQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{project_id}/docs/report")
public class WeeklyReportDocsQueryController {

    private final WeeklyReportQueryService weeklyReportQueryService;

    public WeeklyReportDocsQueryController(WeeklyReportQueryService weeklyReportQueryService) {
        this.weeklyReportQueryService = weeklyReportQueryService;
    }

    // 주간 보고 목록 조회 (Pageable)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> getReports(
            @PathVariable("project_id") Long projectId,
            Pageable pageable
    ) {
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.getDocsReports(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> searchReports(
            @PathVariable("project_id") Long projectId,
            @RequestParam(required = false) WeeklyReport.ReportStatus reportStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartTo,
            Pageable pageable
    ) {
        WeeklyReportSearchCondition condition = new WeeklyReportSearchCondition(
                projectId,
                null,
                reportStatus,
                weekStartFrom,
                weekStartTo
        );
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.searchDocsReports(projectId, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<WeeklyReportDetailResponse>> getReportDetail(
            @PathVariable("project_id") Long projectId,
            @PathVariable Long reportId
    ) {
        WeeklyReportDetailResponse response = weeklyReportQueryService.getDocsReportDetail(projectId, reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 작성하지 않은 주간 보고 주차 목록
    @GetMapping("/missing")
    public ResponseEntity<ApiResponse<List<WeeklyReportMissingResponse>>> getMissingWeeks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("project_id") Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<WeeklyReportMissingResponse> response =
                weeklyReportQueryService.getMissingWeeks(principal, projectId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

package com.moirai.alloc.report.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import com.moirai.alloc.report.query.service.WeeklyReportQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/docs/report")
public class WeeklyReportDocsQueryController {

    private final WeeklyReportQueryService weeklyReportQueryService;

    public WeeklyReportDocsQueryController(WeeklyReportQueryService weeklyReportQueryService) {
        this.weeklyReportQueryService = weeklyReportQueryService;
    }

    // 주간 보고 목록 조회 (Pageable)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> getReports(
            @RequestParam(required = false) Long projectId,
            Pageable pageable
    ) {
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.getDocsReports(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> searchReports(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) WeeklyReport.ReportStatus reportStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartTo,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        WeeklyReportSearchCondition condition = new WeeklyReportSearchCondition(
                projectId,
                userId,
                reportStatus,
                weekStartFrom,
                weekStartTo,
                keyword
        );
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.searchDocsReports(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 상세 조회
    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<WeeklyReportDetailResponse>> getReportDetail(@PathVariable Long reportId) {
        WeeklyReportDetailResponse response = weeklyReportQueryService.getDocsReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

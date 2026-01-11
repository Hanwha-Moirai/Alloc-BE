package com.moirai.alloc.report.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.command.dto.CreateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.DeleteWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.UpdateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.WeeklyReportDeleteResponse;
import com.moirai.alloc.report.command.dto.WeeklyReportSaveResponse;
import com.moirai.alloc.report.command.service.WeeklyReportCommandService;
import com.moirai.alloc.report.query.dto.WeeklyReportCreateResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import com.moirai.alloc.report.query.service.WeeklyReportQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/docs/report")
public class WeeklyReportDocsController {

    private final WeeklyReportQueryService weeklyReportQueryService;
    private final WeeklyReportCommandService weeklyReportCommandService;

    public WeeklyReportDocsController(WeeklyReportQueryService weeklyReportQueryService,
                                      WeeklyReportCommandService weeklyReportCommandService) {
        this.weeklyReportQueryService = weeklyReportQueryService;
        this.weeklyReportCommandService = weeklyReportCommandService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> getReports(
            @RequestParam(required = false) Long projectId,
            Pageable pageable
    ) {
        Page<WeeklyReportSummaryResponse> response =
                weeklyReportQueryService.getDocsReports(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<WeeklyReportSummaryResponse>>> searchReports(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) com.moirai.alloc.report.command.domain.entity.WeeklyReport.ReportStatus reportStatus,
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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WeeklyReportCreateResponse>> createReport(
            @RequestBody CreateWeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportCreateResponse response = weeklyReportCommandService.createWeeklyReport(request, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<WeeklyReportDetailResponse>> getReportDetail(@PathVariable Long reportId) {
        WeeklyReportDetailResponse response = weeklyReportQueryService.getDocsReportDetail(reportId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/save")
    public ResponseEntity<ApiResponse<WeeklyReportSaveResponse>> saveReport(
            @RequestBody UpdateWeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportSaveResponse response = weeklyReportCommandService.updateWeeklyReport(request, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<WeeklyReportDeleteResponse>> deleteReport(
            @RequestBody DeleteWeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        weeklyReportCommandService.deleteWeeklyReport(request.reportId(), principal);
        WeeklyReportDeleteResponse response = new WeeklyReportDeleteResponse(request.reportId(), true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{reportId}/print")
    public ResponseEntity<Void> printReport(@PathVariable Long reportId) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "PDF 출력은 현재 지원하지 않습니다.");
    }
}

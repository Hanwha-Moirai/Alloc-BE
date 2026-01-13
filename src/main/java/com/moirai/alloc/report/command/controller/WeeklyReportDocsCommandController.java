package com.moirai.alloc.report.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.command.dto.request.CreateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.request.DeleteWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.request.UpdateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.response.WeeklyReportDeleteResponse;
import com.moirai.alloc.report.command.dto.response.WeeklyReportSaveResponse;
import com.moirai.alloc.report.command.service.WeeklyReportCommandService;
import com.moirai.alloc.report.query.dto.WeeklyReportCreateResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/projects/{project_id}/docs/report")
public class WeeklyReportDocsCommandController {

    private final WeeklyReportCommandService weeklyReportCommandService;

    public WeeklyReportDocsCommandController(WeeklyReportCommandService weeklyReportCommandService) {
        this.weeklyReportCommandService = weeklyReportCommandService;
    }

    // 주간 보고 생성
    @PostMapping("/create")
    @PreAuthorize("hasRole('PM')")
    public ResponseEntity<ApiResponse<WeeklyReportCreateResponse>> createReport(
            @PathVariable("project_id") Long projectId,
            @RequestBody CreateWeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportCreateResponse response =
                weeklyReportCommandService.createWeeklyReport(projectId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 수정 및 저장
    @PatchMapping("/save")
    @PreAuthorize("hasRole('PM')")
    public ResponseEntity<ApiResponse<WeeklyReportSaveResponse>> saveReport(
            @PathVariable("project_id") Long projectId,
            @RequestBody UpdateWeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WeeklyReportSaveResponse response =
                weeklyReportCommandService.updateWeeklyReport(projectId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 삭제
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('PM')")
    public ResponseEntity<ApiResponse<WeeklyReportDeleteResponse>> deleteReport(
            @PathVariable("project_id") Long projectId,
            @RequestBody DeleteWeeklyReportRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        weeklyReportCommandService.deleteWeeklyReport(projectId, request.reportId(), principal);
        WeeklyReportDeleteResponse response = new WeeklyReportDeleteResponse(request.reportId(), true);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주간 보고 PDF 출력
    @PostMapping("/{reportId}/print")
    public ResponseEntity<Void> printReport(@PathVariable Long reportId) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "PDF 출력은 현재 지원하지 않습니다.");
    }
}

package com.moirai.alloc.report.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.report.query.service.WeeklyReportLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{project_id}/docs/report/logs")
public class WeeklyReportLogController {

    private final WeeklyReportLogQueryService logQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getLogs(
            @PathVariable("project_id") Long projectId,
            @RequestParam(required = false) Integer size
    ) {
        List<String> logs = logQueryService.getLogs(projectId, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}

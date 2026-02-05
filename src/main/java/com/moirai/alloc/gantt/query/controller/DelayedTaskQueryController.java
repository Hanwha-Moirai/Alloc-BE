package com.moirai.alloc.gantt.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.gantt.query.application.DelayedTaskQueryService;
import com.moirai.alloc.gantt.query.dto.request.DelayedTaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.DelayedTaskResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class DelayedTaskQueryController {

    private final DelayedTaskQueryService delayedTaskQueryService;

    public DelayedTaskQueryController(DelayedTaskQueryService delayedTaskQueryService) {
        this.delayedTaskQueryService = delayedTaskQueryService;
    }

    @GetMapping("/delayed")
    public ApiResponse<List<DelayedTaskResponse>> findDelayedTasks(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) java.time.LocalDate from,
            @RequestParam(required = false) java.time.LocalDate to,
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String assigneeName,
            @RequestParam(required = false) Integer delayedDays
    ) {
        DelayedTaskSearchRequest request = new DelayedTaskSearchRequest(
                projectId,
                from,
                to,
                taskName,
                projectName,
                assigneeName,
                delayedDays
        );
        List<DelayedTaskResponse> responses = delayedTaskQueryService.findDelayedTasks(request);
        return ApiResponse.success(responses);
    }
}

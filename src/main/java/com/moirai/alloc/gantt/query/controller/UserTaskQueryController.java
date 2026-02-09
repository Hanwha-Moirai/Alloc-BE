package com.moirai.alloc.gantt.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.gantt.query.application.GanttQueryService;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/tasks")
public class UserTaskQueryController {

    private final GanttQueryService ganttQueryService;

    public UserTaskQueryController(GanttQueryService ganttQueryService) {
        this.ganttQueryService = ganttQueryService;
    }

    @GetMapping
    public ApiResponse<List<TaskResponse>> findIncompleteTasksByUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<TaskResponse> tasks = ganttQueryService.findIncompleteTasksByUserId(principal.userId());
        return ApiResponse.success(tasks);
    }
}

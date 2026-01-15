package com.moirai.alloc.gantt.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.gantt.query.application.GanttQueryService;
import com.moirai.alloc.gantt.query.dto.request.TaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.MilestoneResponse;
import com.moirai.alloc.gantt.query.dto.response.TaskResponse;
import com.moirai.alloc.gantt.command.domain.entity.Task.TaskStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class GanttQueryController {

    private final GanttQueryService ganttQueryService;

    public GanttQueryController(GanttQueryService ganttQueryService) {
        this.ganttQueryService = ganttQueryService;
    }

    // 태스크 조회
    @GetMapping("/tasks")
    public ApiResponse<List<TaskResponse>> findTasks(@PathVariable Long projectId,
                                                     @RequestParam(required = false) String userName,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) LocalDate startDate,
                                                     @RequestParam(required = false) LocalDate endDate) {
        TaskSearchRequest request = new TaskSearchRequest(
                userName,
                status == null ? null : TaskStatus.valueOf(status),
                startDate,
                endDate
        );
        List<TaskResponse> tasks = ganttQueryService.findTasks(projectId, request);
        return ApiResponse.success(tasks);
    }

    // 마일스톤 상세 조회
    @GetMapping("/ganttchart/milestones/{milestoneId}")
    public ApiResponse<MilestoneResponse> findMilestone(@PathVariable Long projectId,
                                                        @PathVariable Long milestoneId) {
        MilestoneResponse milestone = ganttQueryService.findMilestone(projectId, milestoneId);
        return ApiResponse.success(milestone);
    }

    // 마일스톤 목록 조회
    @GetMapping("/ganttchart/milestones")
    public ApiResponse<List<MilestoneResponse>> findMilestones(@PathVariable Long projectId) {
        List<MilestoneResponse> milestones = ganttQueryService.findMilestones(projectId);
        return ApiResponse.success(milestones);
    }
}

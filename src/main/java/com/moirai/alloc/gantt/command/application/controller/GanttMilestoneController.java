package com.moirai.alloc.gantt.command.application.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.gantt.command.application.GanttCommandService;
import com.moirai.alloc.gantt.command.application.dto.request.CreateMilestoneRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateMilestoneRequest;
import com.moirai.alloc.gantt.command.application.dto.response.CreatedIdResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/ganttchart/milestones")
public class GanttMilestoneController {

    private final GanttCommandService ganttCommandService;

    public GanttMilestoneController(GanttCommandService ganttCommandService) {
        this.ganttCommandService = ganttCommandService;
    }

    @PostMapping
    public ApiResponse<CreatedIdResponse> createMilestone(@PathVariable Long projectId,
                                                          @RequestBody CreateMilestoneRequest request) {
        Long milestoneId = ganttCommandService.createMilestone(projectId, request);
        return ApiResponse.success(new CreatedIdResponse(milestoneId));
    }

    @PatchMapping("/{milestoneId}")
    public ApiResponse<Void> updateMilestone(@PathVariable Long projectId,
                                             @PathVariable Long milestoneId,
                                             @RequestBody UpdateMilestoneRequest request) {
        ganttCommandService.updateMilestone(projectId, milestoneId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{milestoneId}")
    public ApiResponse<Void> deleteMilestone(@PathVariable Long projectId,
                                             @PathVariable Long milestoneId) {
        ganttCommandService.deleteMilestone(projectId, milestoneId);
        return ApiResponse.success(null);
    }
}

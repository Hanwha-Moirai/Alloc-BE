package com.moirai.alloc.gantt.command.application.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.gantt.command.application.service.GanttCommandService;
import com.moirai.alloc.gantt.command.application.dto.request.CompleteTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.CreateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.request.UpdateTaskRequest;
import com.moirai.alloc.gantt.command.application.dto.response.CreatedIdResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class GanttTaskController {

    private final GanttCommandService ganttCommandService;

    public GanttTaskController(GanttCommandService ganttCommandService) {
        this.ganttCommandService = ganttCommandService;
    }

    // 태스크 생성
    @PostMapping
    @PreAuthorize("hasRole('PM')")
    public ApiResponse<CreatedIdResponse> createTask(@PathVariable Long projectId,
                                                     @RequestBody CreateTaskRequest request) {
        Long taskId = ganttCommandService.createTask(projectId, request);
        return ApiResponse.success(new CreatedIdResponse(taskId));
    }

    // 태스크 수정
    @PatchMapping("/{taskId}")
    @PreAuthorize("hasRole('PM')")
    public ApiResponse<Void> updateTask(@PathVariable Long projectId,
                                        @PathVariable Long taskId,
                                        @RequestBody UpdateTaskRequest request) {
        ganttCommandService.updateTask(projectId, taskId, request);
        return ApiResponse.success(null);
    }

    // 태스크 삭제
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('PM')")
    public ApiResponse<Void> deleteTask(@PathVariable Long projectId,
                                        @PathVariable Long taskId) {
        ganttCommandService.deleteTask(projectId, taskId);
        return ApiResponse.success(null);
    }

    // 태스크 완료 (태스크 상태 변경)
    @PatchMapping("/{taskId}/complete")
    public ApiResponse<Void> completeTask(@PathVariable Long projectId,
                                          @PathVariable Long taskId,
                                          @RequestBody(required = false) CompleteTaskRequest request) {
        ganttCommandService.completeTask(projectId, taskId, request);
        return ApiResponse.success(null);
    }
}

package com.moirai.alloc.admin.command.controller;

import com.moirai.alloc.admin.command.dto.request.AdminJobSaveRequest;
import com.moirai.alloc.admin.command.service.AdminJobCommandService;
import com.moirai.alloc.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/jobs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobCommandController{

    private final AdminJobCommandService service;

    // 직무 등록
    @PostMapping
    public ApiResponse<Long> createJob(
            @Valid @RequestBody AdminJobSaveRequest request
    ) {
        Long jobId = service.createJob(request.getJobName());
        return ApiResponse.success(jobId);
    }

    // 직무 수정
    @PatchMapping("/{job_id}")
    public ApiResponse<Long> updateJob(
            @PathVariable("job_id") Long jobId,
            @Valid @RequestBody AdminJobSaveRequest request
    ) {
        Long updateJobId = service.updateJob(jobId, request.getJobName());
        return ApiResponse.success(updateJobId);
    }

    // 직무 삭제
    @DeleteMapping("/{job_id}")
    public ApiResponse<Long> deleteJob(
            @PathVariable("job_id") Long jobId
    ) {
        Long deletedJobId = service.deleteJob(jobId);
        return ApiResponse.success(deletedJobId);
    }

}

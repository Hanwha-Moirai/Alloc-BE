package com.moirai.alloc.admin.command.controller;

import com.moirai.alloc.admin.command.dto.request.AdminTechStackSaveRequest;
import com.moirai.alloc.admin.command.service.AdminTechStackCommandService;
import com.moirai.alloc.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tech-stacks")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTechStackCommandController {

    private final AdminTechStackCommandService service;

    // 기술 스택 등록
    @PostMapping
    public ApiResponse<Long> createTechStack(
            @Valid @RequestBody AdminTechStackSaveRequest request
    ) {
        Long techStackId = service.createTechStack(request.getTechName());
        return ApiResponse.success(techStackId);
    }

    // 기술 스택 수정
    @PatchMapping("/{stack_id}")
    public ApiResponse<Long> updateTechStack(
            @PathVariable("stack_id") Long stackId,
            @Valid @RequestBody AdminTechStackSaveRequest request
    ) {
        Long techStackId = service.updateTechStack(stackId, request.getTechName());
        return ApiResponse.success(techStackId);
    }

    // 기술 스택 삭제
    @DeleteMapping("/{stack_id}")
    public ApiResponse<Long> deleteTechStack(
            @PathVariable("stack_id") Long stackId
    ) {
        Long techStackId = service.deleteTechStack(stackId);
        return ApiResponse.success(techStackId);
    }
}

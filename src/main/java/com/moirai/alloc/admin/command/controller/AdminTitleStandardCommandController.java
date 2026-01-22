package com.moirai.alloc.admin.command.controller;

import com.moirai.alloc.admin.command.dto.request.AdminTitleStandardSaveRequest;
import com.moirai.alloc.admin.command.service.AdminTitleStandardCommandService;
import com.moirai.alloc.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/titles")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTitleStandardCommandController {

    private final AdminTitleStandardCommandService service;

    @PostMapping
    public ApiResponse<Long> createTitle(@Valid @RequestBody AdminTitleStandardSaveRequest request) {
        Long id = service.createTitle(request.getTitleName(), request.getMonthlyCost());
        return ApiResponse.success(id);
    }

    @PatchMapping("/{title_id}")
    public ApiResponse<Long> updateTitle(
            @PathVariable("title_id") Long titleId,
            @Valid @RequestBody AdminTitleStandardSaveRequest request
    ) {
        Long id = service.updateTitle(titleId, request.getTitleName(), request.getMonthlyCost());
        return ApiResponse.success(id);
    }
}

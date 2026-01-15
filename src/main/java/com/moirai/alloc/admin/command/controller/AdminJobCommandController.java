package com.moirai.alloc.admin.command.controller;

import com.moirai.alloc.admin.query.dto.AdminJobListItem;
import com.moirai.alloc.admin.query.service.AdminJobQueryService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/jobs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobCommandController {

    private final AdminJobQueryService service;

    @GetMapping
    public ApiResponse<PageResponse<AdminJobListItem>> getJobs(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String q
    ) {
        return ApiResponse.success(service.getJobs(page, size, q));
    }
}

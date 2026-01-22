package com.moirai.alloc.admin.query.controller;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
import com.moirai.alloc.admin.query.service.AdminTechStackQueryService;
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
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTechStackQueryController {

    private final AdminTechStackQueryService service;

    //기술 스택 조회
    @GetMapping("/tech-stacks")
    public ApiResponse<PageResponse<AdminTechStackListItem>> getTechStacks(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String q
    ) {
        return ApiResponse.success(service.getTechStacks(page, size, q));
    }
}

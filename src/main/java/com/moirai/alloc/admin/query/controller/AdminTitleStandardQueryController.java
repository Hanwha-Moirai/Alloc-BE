package com.moirai.alloc.admin.query.controller;

import com.moirai.alloc.admin.query.dto.AdminTitleStandardListItem;
import com.moirai.alloc.admin.query.service.AdminTitleStandardQueryService;
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
public class AdminTitleStandardQueryController {

    private final AdminTitleStandardQueryService service;

    //직급 조회
    @GetMapping("/title_standard")
    public ApiResponse<PageResponse<AdminTitleStandardListItem>> getTechStacks(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String q
    ) {
        return ApiResponse.success(service.getTitleStandard(page, size, q));
    }
}

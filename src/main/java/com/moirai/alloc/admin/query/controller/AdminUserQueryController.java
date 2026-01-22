package com.moirai.alloc.admin.query.controller;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
import com.moirai.alloc.admin.query.dto.AdminUserListItem;
import com.moirai.alloc.admin.query.dto.AdminUserMetaResponse;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.dto.PageResponse;
import com.moirai.alloc.admin.query.service.AdminUserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserQueryController {

    private final AdminUserQueryService service;

    // 사용자 조회
    @GetMapping
    public ApiResponse<PageResponse<AdminUserListItem>> getUsers(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success(service.getUsers(page, size, q, role, status));
    }

    // 사용자 등록/수정 시 메타 데이터 조회(드롭다운)
    @GetMapping("/meta")
    public ApiResponse<AdminUserMetaResponse> getUserMeta() {
        return ApiResponse.success(service.getUserMeta());
    }
}
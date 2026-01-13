package com.moirai.alloc.admin.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.dto.PageResponse;
import com.moirai.alloc.user.query.dto.UserListItem;
import com.moirai.alloc.user.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminQueryController {

    private final UserQueryService userQueryService;

    @GetMapping
    public ApiResponse<PageResponse<UserListItem>> getUsers(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role
    ) {
        return ApiResponse.success(userQueryService.getUsers(page, size, q, role));
    }
}
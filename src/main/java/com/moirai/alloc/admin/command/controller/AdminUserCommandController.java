package com.moirai.alloc.admin.command.controller;

import com.moirai.alloc.admin.command.dto.request.AdminUserCreateRequest;
import com.moirai.alloc.admin.command.dto.request.AdminUserUpdateRequest;
import com.moirai.alloc.admin.command.dto.response.AdminUserResponse;
import com.moirai.alloc.admin.command.service.AdminUserCommandService;
import com.moirai.alloc.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserCommandController {

    private final AdminUserCommandService service;

    @PostMapping
    public ApiResponse<AdminUserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @PatchMapping("/{userId}")
    public ApiResponse<AdminUserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ApiResponse.success(service.updateUser(userId, request));
    }
}

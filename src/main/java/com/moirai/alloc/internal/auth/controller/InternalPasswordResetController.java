package com.moirai.alloc.internal.auth.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.internal.auth.dto.InternalPasswordResetRequest;
import com.moirai.alloc.internal.auth.service.InternalPasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
public class InternalPasswordResetController {

    private final InternalPasswordResetService service;

    @PostMapping("/password/reset")
    @PreAuthorize("hasAuthority('INTERNAL')")
    public ApiResponse<Void> resetPassword(@RequestBody InternalPasswordResetRequest request) {
        service.resetPassword(request.email(), request.newPassword());
        return ApiResponse.success(null);
    }
}

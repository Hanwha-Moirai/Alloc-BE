package com.moirai.alloc.calendar.command.controller;

import com.moirai.alloc.calendar.command.dto.response.AllocationSyncResponse;
import com.moirai.alloc.calendar.command.service.AllocationSyncService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/allocations")
@RequiredArgsConstructor
public class AllocationSyncController {

    private final AllocationSyncService allocationSyncService;

    @PostMapping("/sync-to-calendar")
    @PreAuthorize("hasRole('PM')")
    public ResponseEntity<ApiResponse<AllocationSyncResponse>> syncToCalendar(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                allocationSyncService.syncToCalendar(projectId, principal)
        ));
    }
}

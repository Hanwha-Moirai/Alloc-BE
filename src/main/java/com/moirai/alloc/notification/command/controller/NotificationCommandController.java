package com.moirai.alloc.notification.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationCommandController {

    private final NotificationCommandService commandService;

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = requireUserId(principal);
        commandService.markRead(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);
        commandService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = requireUserId(principal);
        commandService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @DeleteMapping("/read")
    public ResponseEntity<ApiResponse<Void>> deleteAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);
        commandService.deleteAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 없습니다.");
        }
        return principal.userId().longValue();
    }
}

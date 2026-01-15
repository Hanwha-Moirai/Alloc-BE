package com.moirai.alloc.notification.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.notification.query.dto.response.NotificationPageResponse;
import com.moirai.alloc.notification.query.service.NotificationQueryService;
import com.moirai.alloc.notification.query.sse.NotificationSseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationQueryController {

    private final NotificationQueryService queryService;
    private final NotificationSseEmitters emitters;

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @GetMapping("/stream")
    public SseEmitter stream(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);

        SseEmitter emitter = emitters.add(userId);

        try {
            long unread = queryService.getMyUnreadCount(userId);
            emitters.sendToUser(userId, "UNREAD_COUNT", unread);
        } catch (Exception ignored) {}

        return emitter;
    }

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = requireUserId(principal);
        var res = queryService.getMyNotifications(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getMyUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(queryService.getMyUnreadCount(userId)));
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 없습니다.");
        }
        return principal.userId().longValue();
    }
}

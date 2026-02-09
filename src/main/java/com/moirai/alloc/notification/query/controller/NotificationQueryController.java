package com.moirai.alloc.notification.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.notification.query.dto.response.NotificationPageResponse;
import com.moirai.alloc.notification.query.dto.response.NotificationPollResponse;
import com.moirai.alloc.notification.query.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationQueryController {

    private final NotificationQueryService queryService;

    /**
     * [알림함] 내 알림 목록 조회(페이징)
     * - 요구사항: "알림 목록 조회"
     * - 조건: deleted=false만 노출(soft delete 반영)
     * - 정렬: created_at DESC
     */
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

    /**
     * [알림함] 내 미읽음 알림 개수
     * - 요구사항: "미읽음 알림 개수 표시"
     * - 조건: read=false and deleted=false
     */
    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getMyUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(queryService.getMyUnreadCount(userId)));
    }

    /**
     * [알림함] 폴링 기반 신규 알림 조회
     * - 기준: sinceId 이후 신규 알림만 반환
     * - 응답: 신규 알림 목록 + 미읽음 카운트 + latestNotificationId
     */
    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @GetMapping("/poll")
    public ResponseEntity<ApiResponse<NotificationPollResponse>> poll(
            @RequestParam(defaultValue = "0") Long sinceId,
            @RequestParam(required = false) Integer size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.success(queryService.pollMyNotifications(userId, sinceId, size)));
    }

    /**
     * AuthenticationPrincipal에서 userId 확보(미인증이면 403)
     */
    private Long requireUserId(UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 없습니다.");
        }
        return principal.userId().longValue();
    }
}

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

    /**
     * [SSE] 알림 스트림 구독
     * - 요구사항: "알림 수신(SSE)", "미읽음 알림 개수 표시"
     * - 동작: emitter 등록 후, 초기 미읽음 카운트를 즉시 1회 전송(UX: 뱃지 초기화)
     * - 이후: AlarmCreated/UnreadChanged 이벤트가 발생하면 SSE로 NOTIFICATION/UNREAD_COUNT push
     */
    @PreAuthorize("hasAnyRole('ADMIN','PM','USER')")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = requireUserId(principal);

        // 초기 unread 카운트 1회 전송(실패해도 구독 자체는 유지)
        Long unread = null;
        try {
            unread = queryService.getMyUnreadCount(userId);
        } catch (Exception ignored) {
            unread = null;
        }

        SseEmitter emitter = emitters.add(userId);

        if (unread != null) {
            try {
                emitters.sendToUser(userId, "UNREAD_COUNT", unread);
            } catch (Exception ignored) {}
        }

        return emitter;
    }

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
     * AuthenticationPrincipal에서 userId 확보(미인증이면 403)
     */
    private Long requireUserId(UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 없습니다.");
        }
        return principal.userId().longValue();
    }
}

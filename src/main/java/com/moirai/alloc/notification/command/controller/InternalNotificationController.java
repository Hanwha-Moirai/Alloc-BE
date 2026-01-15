package com.moirai.alloc.notification.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.notification.command.dto.request.InternalNotificationCreateRequest;
import com.moirai.alloc.notification.command.dto.response.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.service.NotificationCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/notifications")
public class InternalNotificationController {

    private final NotificationCommandService commandService;

    /**
     * 시스템/도메인 서비스들이 호출하는 내부 알림 생성 API
     * Gateway 내부 인증(서비스 토큰/API KEY 등)으로 보호 권장
     */
    @PreAuthorize("hasAuthority('INTERNAL')")
    @PostMapping
    public ResponseEntity<ApiResponse<InternalNotificationCreateResponse>> create(@Valid @RequestBody InternalNotificationCreateRequest request) {
        var res = commandService.createInternalNotifications(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }
}

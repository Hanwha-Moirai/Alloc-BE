package com.moirai.alloc.notification.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
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
     * [시스템 내부] 알림 생성(수신자 N명 → alarm_log N건 + alarm_send_log N건)
     * - 요구사항: "알림 이벤트 생성" (프로젝트 할당/태스크 담당자 배정/일정 초대 등 트리거)
     * - 템플릿: alarm_template(타입별) 조회 후 variables 치환하여 title/body 생성
     * - 응답: 생성된 alarm_id 목록 및 createdCount 반환
     * - 보안: 외부 노출 금지. 게이트웨이/서비스토큰 등으로 INTERNAL 권한 보호 권장
     */
    @PreAuthorize("hasAuthority('INTERNAL')")
    @PostMapping
    public ResponseEntity<ApiResponse<InternalNotificationCreateResponse>> create(
            @Valid @RequestBody InternalNotificationCommand cmd
    ) {
        var res = commandService.createInternalNotifications(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }
}

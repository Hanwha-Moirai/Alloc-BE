package com.moirai.alloc.calendar.command.controller;

import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.calendar.command.service.CalendarService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/calendar/events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarService calendarService;

    /**
     * [API 기능] 공유 일정 이벤트 생성 (PM)
     * POST /api/projects/{projectId}/calendar/events/shared
     *
     * - PM만 생성 가능
     * - 참여자(memberUserIds) 필수
     * - 기간 유효성(시작 < 종료), 프로젝트 멤버십/권한 검증은 Service에서 수행
     */
    @PostMapping("/shared")
    @PreAuthorize("hasRole('PM')")
    public ResponseEntity<ApiResponse<EventResponse>> createSharedEvent(
            @PathVariable Long projectId,
            @Valid @RequestBody SharedEventCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.createSharedEvent(projectId, request, principal)
        ));
    }

    /**
     * [API 기능] 개인 일정 이벤트 생성 (PM, USER)
     * POST /api/projects/{projectId}/calendar/events/personal
     *
     * - 개인 일정(PRIVATE): 생성한 사용자 본인에게만 보이도록 처리
     * - 기간 유효성/프로젝트 멤버십 검증은 Service에서 수행
     */
    @PostMapping("/personal")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<EventResponse>> createPersonalEvent(
            @PathVariable Long projectId,
            @Valid @RequestBody PersonalEventCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.createPersonalEvent(projectId, request, principal)
        ));
    }

    /**
     * [API 기능] 휴가 일정 이벤트 생성 (PM, USER)
     * POST /api/projects/{projectId}/calendar/events/vacation
     *
     * - 휴가 일정(VACATION): 일반 사용자도 생성 가능
     * - eventName 미입력 시 기본값("휴가") 처리 등은 Service에서 수행
     */
    @PostMapping("/vacation")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<EventResponse>> createVacationEvent(
            @PathVariable Long projectId,
            @Valid @RequestBody VacationEventCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.createVacationEvent(projectId, request, principal)
        ));
    }

    /**
     * [API 기능] 일정 이벤트 달성 여부 변경 (PM, USER)
     * PATCH /api/projects/{projectId}/calendar/events/{eventId}/completion
     *
     * - completed=true  -> SUCCESS
     * - completed=false -> IN_PROGRESS
     * - 수정 권한(작성자/PM 등) 및 프로젝트 멤버십 검증은 Service에서 수행
     */
    @PatchMapping("/{eventId}/completion")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<EventResponse>> updateCompletion(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventCompletionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.updateCompletion(projectId, eventId, request, principal)
        ));
    }

    /**
     * [API 기능] 일정 이벤트 수정 (PM, USER)
     * PATCH /api/projects/{projectId}/calendar/events/{eventId}
     *
     * - 부분 수정(널 필드는 미수정) 방식
     * - PUBLIC(공유) 변경 시 참여자(memberUserIds) 필수 규칙, 타입 변경 권한 규칙 등은 Service에서 수행
     * - PUBLIC -> PRIVATE/VACATION 전환 시 참여자 테이블 정리(오염 방지) 등도 Service에서 처리
     */
    @PatchMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.updateEvent(projectId, eventId, request, principal)
        ));
    }

    /**
     * [API 기능] 일정 이벤트 삭제 (PM, USER)
     * DELETE /api/projects/{projectId}/calendar/events/{eventId}
     *
     * - Soft delete(is_deleted=true)
     * - 공유 일정인 경우 참여자 매핑 데이터도 정리
     * - 삭제 권한(작성자/PM 등) 및 프로젝트 멤버십 검증은 Service에서 수행
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        calendarService.deleteEvent(projectId, eventId, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 이벤트 상세 조회
     * GET /api/projects/{projectId}/calendar/events/{eventId}
     *
     * - PUBLIC(공유) 일정이면 참여자 ID 목록 + 참여자 이름 목록을 포함하여 반환
     * - PRIVATE(개인) 일정은 작성자만 조회 가능
     * - 프로젝트 멤버십 검증은 Service에서 수행
     */
    @GetMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEventDetail(
            @PathVariable Long projectId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.getEventDetail(projectId, eventId, principal)
        ));
    }
}

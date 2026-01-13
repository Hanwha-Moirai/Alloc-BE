package com.moirai.alloc.calendar.command.controller;

import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.calendar.command.service.CalendarService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/calendar/events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarService calendarService;

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

    @PostMapping("/vacation")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<EventResponse>> createVacationEvent(
            @PathVariable Long projectId,
            @Valid
            @RequestBody VacationEventCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.createVacationEvent(projectId, request, principal)
        ));
    }

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

    /** 이벤트 상세 조회: 공유 일정이면 참여자 포함 */
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

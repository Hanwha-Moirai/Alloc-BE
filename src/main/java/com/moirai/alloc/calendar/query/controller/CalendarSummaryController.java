package com.moirai.alloc.calendar.query.controller;

import com.moirai.alloc.calendar.query.dto.TodayEventsResponse;
import com.moirai.alloc.calendar.query.dto.WeeklyEventCountResponse;
import com.moirai.alloc.calendar.query.service.CalendarQueryService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarSummaryController {

    private final CalendarQueryService calendarQueryService;

    /**
     * [API 기능] 이번 주(월~일) 내가 볼 수 있는 이벤트 개수 조회 (PM, USER)
     * GET /api/calendar/weekly-count
     *
     * - 사용자의 ASSIGNED 상태 프로젝트들을 기준으로 "노출 가능한 이벤트"를 집계
     * - PRIVATE는 본인 소유만, PUBLIC/VACATION은 규칙에 따라 노출되는 이벤트만 포함
     */
    @GetMapping("/weekly-count")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<WeeklyEventCountResponse>> getMyWeeklyCount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarQueryService.getMyWeeklyEventCount(principal)
        ));
    }

    /**
     * [API 기능] 오늘 내가 볼 수 있는 이벤트 목록 조회 (PM, USER)
     * GET /api/calendar/today
     *
     * - 사용자의 ASSIGNED 상태 프로젝트들을 기준으로 "오늘 범위"에 걸치는 이벤트만 조회
     * - 커서 기반 페이징 지원(cursorStart + cursorId를 함께 전달)
     * - nextCursorStart/nextCursorId가 있으면 다음 페이지 조회 가능
     */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<TodayEventsResponse>> getMyTodayEvents(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorStart,
            @RequestParam(required = false) Long cursorId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarQueryService.getMyTodayEvents(limit, cursorStart, cursorId, principal)
        ));
    }
}

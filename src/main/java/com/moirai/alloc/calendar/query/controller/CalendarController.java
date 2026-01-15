package com.moirai.alloc.calendar.query.controller;

import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.query.service.CalendarQueryService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/projects/{projectId}/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarQueryService calendarQueryService;

    /**
     * [API 기능] 캘린더 이벤트 뷰 제공 (PM, USER)
     * GET /api/projects/{projectId}/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD&view=month
     *
     * - 기간(from~to) 범위에 해당하는 캘린더 "이벤트(EVENT)"만 조회
     * - EVENT 노출 범위:
     *   PUBLIC/VACATION은 프로젝트 멤버에게 노출, PRIVATE는 본인 소유만 노출
     * - view 파라미터는 확장용(기본 month)이며, 현재 로직에서 이벤트 조회 범위를 바꾸진 않음
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<CalendarViewResponse>> getCalendarView(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "month") String view,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarQueryService.getCalendarView(projectId, from, to, view, principal)
        ));
    }
}

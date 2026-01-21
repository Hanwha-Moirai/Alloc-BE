package com.moirai.alloc.calendar.query.controller;

import com.moirai.alloc.calendar.query.dto.ProjectUpcomingEventsResponse;
import com.moirai.alloc.calendar.query.service.CalendarQueryService;
import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/projects/{projectId}/calendar")
@RequiredArgsConstructor
public class CalendarProjectSummaryController {

    private final CalendarQueryService calendarQueryService;

    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('PM','USER')")
    public ResponseEntity<ApiResponse<ProjectUpcomingEventsResponse>> getUpcoming(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorStart,
            @RequestParam(required = false) Long cursorId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarQueryService.getProjectUpcomingEvents(projectId, limit, cursorStart, cursorId, principal)
        ));
    }
}
package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.query.dto.ProjectUpcomingEventsResponse;
import com.moirai.alloc.calendar.query.dto.TodayEventsResponse;
import com.moirai.alloc.calendar.query.dto.WeeklyEventCountResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface CalendarQueryService {

    /**
     * 캘린더 뷰 조회
     * - 프로젝트 멤버십(ASSIGNED) 검증
     * - 조회 범위(from~to) 유효성 검증
     * - EVENT는 권한에 따라 노출 범위가 다름(PRIVATE는 본인만)
     * - 결과는 시작일시 기준 정렬하여 반환
     */
    CalendarViewResponse getCalendarView(Long projectId, LocalDate from, LocalDate to, String view, UserPrincipal principal);

    WeeklyEventCountResponse getMyWeeklyEventCount(UserPrincipal principal);

    TodayEventsResponse getMyTodayEvents(
            int limit,
            LocalDateTime cursorStart,
            Long cursorId,
            UserPrincipal principal
    );

    ProjectUpcomingEventsResponse getProjectUpcomingEvents(
            Long projectId,
            int limit,
            LocalDateTime cursorStart,
            Long cursorId,
            UserPrincipal principal
    );
}

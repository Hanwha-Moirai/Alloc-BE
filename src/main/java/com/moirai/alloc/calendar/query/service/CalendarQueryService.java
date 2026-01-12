package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;

import java.time.LocalDate;

public interface CalendarQueryService {
    CalendarViewResponse getCalendarView(Long projectId, LocalDate from, LocalDate to, String view, UserPrincipal principal);
}

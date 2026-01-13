package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;

public interface CalendarService {
    EventResponse createSharedEvent(Long projectId, SharedEventCreateRequest request, UserPrincipal principal);

    EventResponse createPersonalEvent(Long projectId, PersonalEventCreateRequest request, UserPrincipal principal);

    EventResponse createVacationEvent(Long projectId, VacationEventCreateRequest request, UserPrincipal principal);

    EventResponse updateCompletion(Long projectId, Long eventId, EventCompletionRequest request, UserPrincipal principal);

    EventResponse updateEvent(Long projectId, Long eventId, EventUpdateRequest request, UserPrincipal principal);

    void deleteEvent(Long projectId, Long eventId, UserPrincipal principal);

    EventDetailResponse getEventDetail(Long projectId, Long eventId, UserPrincipal principal);
}

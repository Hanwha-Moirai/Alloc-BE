package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.calendar.command.dto.response.CalendarEventItemResponse;
import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarQueryServiceImpl implements CalendarQueryService {

    private final EventsRepository eventsRepository;
    private final SquadAssignmentRepository squadAssignmentRepository;

    /**
     * 캘린더 이벤트 뷰 조회 (EVENT only)
     * - from/to 범위를 LocalDateTime으로 확장(from=00:00, to는 +1일 00:00 exclusive)
     * - Events: findVisibleEvents로 권한 필터링(PRIVATE는 본인만)
     * - 시작일시 기준 정렬 후 반환
     */
    @Override
    @Transactional(readOnly = true)
    public CalendarViewResponse getCalendarView(Long projectId, LocalDate from, LocalDate to, String view, UserPrincipal principal) {

        checkProjectMembership(projectId, principal);
        validateDateRange(from, to);

        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        List<CalendarEventItemResponse> items = new ArrayList<>();

        List<Events> events = eventsRepository.findVisibleEvents(projectId, fromStart, toExclusive, principal.userId());
        for (Events event : events) {
            items.add(CalendarEventItemResponse.builder()
                    .eventId(event.getId())
                    .ownerUserId(event.getOwnerUserId())
                    .title(event.getEventName())
                    .start(event.getStartDate())
                    .end(event.getEndDate())
                    .eventType(event.getEventType())
                    .eventState(event.getEventState())
                    .build());
        }

        // 정렬: 시작일시 기준으로 UI 렌더링/타임라인에 유리하게 정렬
        items.sort(Comparator.comparing(CalendarEventItemResponse::getStart));

        return CalendarViewResponse.builder()
                .items(items)
                .build();
    }

    /** 프로젝트 ASSIGNED 멤버인지 검증 (아니면 Forbidden) */
    private void checkProjectMembership(Long projectId, UserPrincipal principal) {
        boolean isMember = squadAssignmentRepository
                .existsByProjectIdAndUserIdAndFinalDecision(projectId, principal.userId(), FinalDecision.ASSIGNED);

        if (!isMember) throw new ForbiddenException("프로젝트 참여자가 아닙니다.");
    }

    /** 조회 기간(from/to) 유효성 검증: null 금지 + from <= to */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("조회 시작일(from)과 종료일(to)은 필수입니다.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일(from)은 종료일(to)보다 이후일 수 없습니다.");
        }
    }
}

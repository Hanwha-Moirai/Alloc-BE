package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.calendar.command.dto.response.CalendarItemResponse;
import com.moirai.alloc.calendar.command.dto.response.CalendarItemType;
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

    @Override
    @Transactional(readOnly = true)
    public CalendarViewResponse getCalendarView(Long projectId, LocalDate from, LocalDate to, String view, UserPrincipal principal) {

        checkProjectMembership(projectId, principal);

        // 요청 파라미터 방어 (선택)
        validateDateRange(from, to);

        LocalDateTime fromStart = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();

        List<CalendarItemResponse> items = new ArrayList<>();

        // 사용자의 권한/노출 정책은 repository의 findVisibleEvents에서 처리한다고 가정
        List<Events> events = eventsRepository.findVisibleEvents(projectId, fromStart, toExclusive, principal.userId());

        for (Events event : events) {
            Map<String, Object> meta = new HashMap<>();
            meta.put("eventType", event.getEventType());
            meta.put("eventState", event.getEventState());

            items.add(CalendarItemResponse.builder()
                    .itemType(CalendarItemType.EVENT)
                    .id(event.getId())
                    .title(event.getEventName())
                    .start(event.getStartDate())
                    .end(event.getEndDate())
                    .colorHint(event.getEventType().name())
                    .meta(meta)
                    .build());
        }

        items.sort(Comparator.comparing(CalendarItemResponse::getStart));

        return CalendarViewResponse.builder()
                .items(items)
                .build();
    }

    /**
     * 프로젝트 참여자(동료/PM 포함)인지 검증
     * - squad_assignment.final_decision = ASSIGNED 를 “프로젝트 참여”로 간주
     */
    private void checkProjectMembership(Long projectId, UserPrincipal principal) {
        boolean isMember = squadAssignmentRepository
                .existsByProjectIdAndUserIdAndFinalDecision(projectId, principal.userId(), FinalDecision.ASSIGNED);

        if (!isMember) {
            throw new ForbiddenException("프로젝트 참여자가 아닙니다.");
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("조회 시작일(from)과 종료일(to)은 필수입니다.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일(from)은 종료일(to)보다 이후일 수 없습니다.");
        }
    }
}

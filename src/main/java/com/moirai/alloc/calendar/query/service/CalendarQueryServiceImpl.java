package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.calendar.command.dto.response.CalendarEventItemResponse;
import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.calendar.query.dto.*;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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


    /**
     * [요약] 이번 주(월~일) 기준으로 "내가 볼 수 있는 이벤트"의 총 개수를 반환
     *
     * - ASSIGNED 상태인 내 프로젝트들만 대상으로 집계
     * - 리포지토리 레벨에서 PRIVATE/PUBLIC/VACATION 노출 규칙을 반영한 count 쿼리를 사용
     */
    @Override
    @Transactional(readOnly = true)
    public WeeklyEventCountResponse getMyWeeklyEventCount(UserPrincipal principal) {
        Long userId = principal.userId();

        // 이번 주 (월~일) 기준: ISO 주차(월 시작)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        LocalDateTime fromStart = weekStart.atStartOfDay();
        LocalDateTime toExclusive = weekEnd.plusDays(1).atStartOfDay();

        long total = eventsRepository.countWeeklyVisibleEventsAcrossMyProjects(
                userId,
                FinalDecision.ASSIGNED,
                fromStart,
                toExclusive
        );

        return new WeeklyEventCountResponse(weekStart, weekEnd, total);
    }

    /**
     * [요약] 오늘(00:00~익일 00:00) 내가 볼 수 있는 이벤트 목록을 커서 기반으로 조회
     *
     * - cursorStart/cursorId는 반드시 함께 전달(둘 중 하나만 오면 400)
     * - limit은 1~100으로 보정
     * - nextCursorStart/nextCursorId가 있으면 다음 페이지를 이어서 조회 가능
     */
    @Override
    @Transactional(readOnly = true)
    public TodayEventsResponse getMyTodayEvents(
            int limit,
            LocalDateTime cursorStart,
            Long cursorId,
            UserPrincipal principal
    ) {
        if ((cursorStart == null) != (cursorId == null)) {
            throw new IllegalArgumentException("cursorStart와 cursorId는 함께 전달되어야 합니다.");
        }

        int pageSize = Math.min(Math.max(limit, 1), 100);
        int requestSize = pageSize + 1;

        Long userId = principal.userId();

        LocalDate today = LocalDate.now();
        LocalDateTime fromStart = today.atStartOfDay();
        LocalDateTime toExclusive = today.plusDays(1).atStartOfDay();

        List<Events> fetched = eventsRepository.findTodayVisibleEventsAcrossMyProjects(
                userId,
                FinalDecision.ASSIGNED,
                fromStart,
                toExclusive,
                cursorStart,
                cursorId,
                PageRequest.of(0, requestSize)
        );

        boolean hasNext = fetched.size() > pageSize;
        List<Events> page = hasNext ? fetched.subList(0, pageSize) : fetched;

        List<TodayEventItemResponse> items = page.stream()
                .map(e -> new TodayEventItemResponse(
                        e.getId(),
                        e.getProjectId(),
                        e.getEventName(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.getEventType()
                ))
                .toList();

        LocalDateTime nextCursorStartVal = null;
        Long nextCursorIdVal = null;

        if (hasNext && !items.isEmpty()) {
            TodayEventItemResponse last = items.get(items.size() - 1);
            nextCursorStartVal = last.start();
            nextCursorIdVal = last.eventId();
        }

        return new TodayEventsResponse(today, items, nextCursorStartVal, nextCursorIdVal);
    }

    /**
     * [요약] 특정 프로젝트 내에서 "현재 이후"의 다가오는 이벤트 목록을 커서 기반으로 조회
     *
     * - 프로젝트 멤버십(ASSIGNED) 검증
     * - cursorStart/cursorId는 반드시 함께 전달(둘 중 하나만 오면 400)
     * - limit은 1~100으로 보정
     * - 응답에는 이벤트 타입별 라벨 및 시작일 기준 D-day를 포함
     */
    @Override
    @Transactional(readOnly = true)
    public ProjectUpcomingEventsResponse getProjectUpcomingEvents(
            Long projectId,
            int limit,
            LocalDateTime cursorStart,
            Long cursorId,
            UserPrincipal principal
    ) {
        checkProjectMembership(projectId, principal);

        // 커서 짝 검증
        if ((cursorStart == null) != (cursorId == null)) {
            throw new IllegalArgumentException("cursorStart와 cursorId는 함께 전달되어야 합니다.");
        }

        int pageSize = Math.min(Math.max(limit, 1), 100); // 1~100
        int requestSize = pageSize + 1;                   // hasNext 판단용
        LocalDateTime now = LocalDateTime.now();

        List<Events> fetched = eventsRepository.findUpcomingVisibleEventsInProject(
                projectId,
                principal.userId(),
                now,
                cursorStart,
                cursorId,
                PageRequest.of(0, requestSize)
        );

        boolean hasNext = fetched.size() > pageSize;
        List<Events> page = hasNext ? fetched.subList(0, pageSize) : fetched;

        LocalDate today = LocalDate.now();

        List<ProjectUpcomingEventItemResponse> items = page.stream()
                .map(e -> new ProjectUpcomingEventItemResponse(
                        e.getId(),
                        e.getEventType(),
                        labelOf(e.getEventType()),
                        e.getEventName(),
                        e.getStartDate(),
                        e.getEndDate(),
                        ChronoUnit.DAYS.between(today, e.getStartDate().toLocalDate())
                ))
                .toList();

        LocalDateTime nextCursorStartVal = null;
        Long nextCursorIdVal = null;

        if (hasNext && !items.isEmpty()) {
            ProjectUpcomingEventItemResponse last = items.get(items.size() - 1);
            nextCursorStartVal = last.start();
            nextCursorIdVal = last.eventId();
        }

        return new ProjectUpcomingEventsResponse(projectId, items, nextCursorStartVal, nextCursorIdVal);
    }

    /**
     * 이벤트 타입을 UI/응답에서 쓰기 좋은 한글 라벨로 변환
     * - PUBLIC   -> "공유 일정"
     * - PRIVATE  -> "개인 일정"
     * - VACATION -> "휴가"
     */
    private String labelOf(com.moirai.alloc.calendar.command.domain.entity.EventType type) {
        return switch (type) {
            case PUBLIC -> "공유 일정";
            case PRIVATE -> "개인 일정";
            case VACATION -> "휴가";
        };
    }
}

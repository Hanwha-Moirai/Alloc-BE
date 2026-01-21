package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.domain.entity.EventState;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.calendar.query.dto.ProjectUpcomingEventsResponse;
import com.moirai.alloc.calendar.query.dto.TodayEventsResponse;
import com.moirai.alloc.calendar.query.dto.WeeklyEventCountResponse;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarQueryServiceImplTest {

    @InjectMocks
    private CalendarQueryServiceImpl calendarQueryService;

    @Mock private EventsRepository eventsRepository;
    @Mock private SquadAssignmentRepository squadAssignmentRepository;

    @Mock private UserPrincipal principal;

    private final Long projectId = 1L;
    private final Long userId = 20L;

    // =========================================================
    // Helper: 최소 스텁 (STRICT_STUBS 대응)
    // =========================================================
    private void stubUserId(Long id) {
        when(principal.userId()).thenReturn(id);
    }

    private void stubProjectMember(boolean isMember) {
        when(squadAssignmentRepository.existsByProjectIdAndUserIdAndFinalDecision(
                eq(projectId), eq(userId), eq(FinalDecision.ASSIGNED)
        )).thenReturn(isMember);
    }

    /** 멤버십 체크가 선행되는 메서드에서만 호출 */
    private void asProjectMember() {
        stubUserId(userId);
        stubProjectMember(true);
    }

    /** 비멤버 Forbidden 케이스에서만 호출 */
    private void asNotProjectMember() {
        stubUserId(userId);
        stubProjectMember(false);
    }

    private static void setEntityId(Object entity, String fieldName, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================
    // getCalendarView
    // =========================================================
    @Test
    @DisplayName("getCalendarView: 성공 -> 이벤트/태스크/마일스톤 통합 + 시작일시 기준 정렬")
    void getCalendarView_success_sorted() {
        asProjectMember();

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        Events e1 = Events.builder()
                .projectId(projectId)
                .ownerUserId(999L)
                .eventName("B")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.of(2026, 1, 10, 10, 0))
                .endDate(LocalDateTime.of(2026, 1, 10, 11, 0))
                .eventDescription("")
                .build();
        setEntityId(e1, "id", 1L);

        Events e2 = Events.builder()
                .projectId(projectId)
                .ownerUserId(999L)
                .eventName("A")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.of(2026, 1, 5, 10, 0))
                .endDate(LocalDateTime.of(2026, 1, 5, 11, 0))
                .eventDescription("")
                .build();
        setEntityId(e2, "id", 2L);

        when(eventsRepository.findVisibleEvents(eq(projectId), any(), any(), eq(userId)))
                .thenReturn(List.of(e1, e2)); // 일부러 역순

        CalendarViewResponse res = calendarQueryService.getCalendarView(projectId, from, to, "month", principal);

        assertThat(res.getItems()).hasSize(2);
        assertThat(res.getItems().get(0).getTitle()).isEqualTo("A");
        assertThat(res.getItems().get(1).getTitle()).isEqualTo("B");
    }

    @Test
    @DisplayName("getCalendarView: from > to 이면 IllegalArgumentException (멤버십 체크가 먼저라서 멤버 스텁 필요)")
    void getCalendarView_fail_invalidRange() {
        // 실제 구현이 '멤버십 체크 -> range 검증' 순서이므로 멤버 스텁 없으면 Forbidden이 먼저 터짐
        asProjectMember();

        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> calendarQueryService.getCalendarView(projectId, from, to, "month", principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from");

        // 멤버십 체크는 수행되지만, range 에러로 repository 조회(findVisibleEvents)는 호출되면 안 됨
        verify(squadAssignmentRepository).existsByProjectIdAndUserIdAndFinalDecision(projectId, userId, FinalDecision.ASSIGNED);
        verifyNoInteractions(eventsRepository);
    }

    @Test
    @DisplayName("getCalendarView: 프로젝트 멤버가 아니면 ForbiddenException")
    void getCalendarView_fail_notMember() {
        asNotProjectMember();

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        assertThatThrownBy(() -> calendarQueryService.getCalendarView(projectId, from, to, "month", principal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("프로젝트 참여자");

        verifyNoInteractions(eventsRepository);
    }

    // =========================================================
    // weekly-count / today / upcoming
    // =========================================================
    @Test
    @DisplayName("getMyWeeklyEventCount: 성공 -> repository count 호출 + 값 매핑 (멤버십 스텁 불필요)")
    void getMyWeeklyEventCount_success() {
        // 이 메서드가 프로젝트 멤버십 체크를 하지 않는 형태라면 userId만 스텁하면 됨
        stubUserId(userId);

        when(eventsRepository.countWeeklyVisibleEventsAcrossMyProjects(
                eq(userId),
                eq(FinalDecision.ASSIGNED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(7L);

        WeeklyEventCountResponse res = calendarQueryService.getMyWeeklyEventCount(principal);

        assertThat(res.total()).isEqualTo(7L);
        verify(eventsRepository).countWeeklyVisibleEventsAcrossMyProjects(eq(userId), eq(FinalDecision.ASSIGNED), any(), any());
    }

    @Test
    @DisplayName("getMyTodayEvents: cursorStart만 있거나 cursorId만 있으면 IllegalArgumentException (검증이 먼저라 스텁 불필요)")
    void getMyTodayEvents_fail_cursorMismatch() {
        assertThatThrownBy(() -> calendarQueryService.getMyTodayEvents(10, LocalDateTime.now(), null, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("함께 전달");

        assertThatThrownBy(() -> calendarQueryService.getMyTodayEvents(10, null, 1L, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("함께 전달");

        verifyNoInteractions(eventsRepository, squadAssignmentRepository);
    }

    @Test
    @DisplayName("getMyTodayEvents: 성공(hasNext=false) -> nextCursor null")
    void getMyTodayEvents_success_noNext() {
        stubUserId(userId);

        Events e1 = Events.builder()
                .projectId(projectId)
                .ownerUserId(999L)
                .eventName("A")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0))
                .endDate(LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0))
                .eventDescription("")
                .build();
        setEntityId(e1, "id", 1L);

        when(eventsRepository.findTodayVisibleEventsAcrossMyProjects(
                eq(userId),
                eq(FinalDecision.ASSIGNED),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                isNull(),
                any()
        )).thenReturn(List.of(e1));

        TodayEventsResponse res = calendarQueryService.getMyTodayEvents(50, null, null, principal);

        assertThat(res.items()).hasSize(1);
        assertThat(res.nextCursorStart()).isNull();
        assertThat(res.nextCursorId()).isNull();
    }

    @Test
    @DisplayName("getMyTodayEvents: 성공(hasNext=true) -> nextCursor가 마지막 아이템 기준으로 세팅")
    void getMyTodayEvents_success_hasNext_setsNextCursor() {
        stubUserId(userId);

        Events e1 = Events.builder()
                .projectId(projectId)
                .ownerUserId(999L)
                .eventName("A")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0))
                .endDate(LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0))
                .eventDescription("")
                .build();
        setEntityId(e1, "id", 1L);

        Events e2 = Events.builder()
                .projectId(projectId)
                .ownerUserId(999L)
                .eventName("B")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.now().withHour(11).withMinute(0).withSecond(0).withNano(0))
                .endDate(LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0))
                .eventDescription("")
                .build();
        setEntityId(e2, "id", 2L);

        Events e3 = Events.builder()
                .projectId(projectId)
                .ownerUserId(999L)
                .eventName("C")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.now().withHour(13).withMinute(0).withSecond(0).withNano(0))
                .endDate(LocalDateTime.now().withHour(14).withMinute(0).withSecond(0).withNano(0))
                .eventDescription("")
                .build();
        setEntityId(e3, "id", 3L);

        when(eventsRepository.findTodayVisibleEventsAcrossMyProjects(
                eq(userId),
                eq(FinalDecision.ASSIGNED),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                isNull(),
                isNull(),
                any()
        )).thenReturn(List.of(e1, e2, e3)); // size=3 => hasNext=true, page=[e1,e2]

        TodayEventsResponse res = calendarQueryService.getMyTodayEvents(2, null, null, principal);

        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(1).eventId()).isEqualTo(2L);
        assertThat(res.nextCursorStart()).isEqualTo(res.items().get(1).start());
        assertThat(res.nextCursorId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getProjectUpcomingEvents: 프로젝트 멤버가 아니면 ForbiddenException")
    void getProjectUpcomingEvents_fail_notMember() {
        asNotProjectMember();

        assertThatThrownBy(() -> calendarQueryService.getProjectUpcomingEvents(projectId, 20, null, null, principal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("프로젝트 참여자");

        verifyNoInteractions(eventsRepository);
    }

    @Test
    @DisplayName("getProjectUpcomingEvents: cursorStart/cursorId 짝이 깨지면 IllegalArgumentException (멤버십 체크가 먼저라 멤버 스텁 필요)")
    void getProjectUpcomingEvents_fail_cursorMismatch() {
        // 실제 구현이 '멤버십 체크 -> cursor 검증' 순서이므로 멤버 스텁 없으면 Forbidden이 먼저 터짐
        asProjectMember();

        assertThatThrownBy(() -> calendarQueryService.getProjectUpcomingEvents(projectId, 20, LocalDateTime.now(), null, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("함께 전달");

        verify(squadAssignmentRepository).existsByProjectIdAndUserIdAndFinalDecision(projectId, userId, FinalDecision.ASSIGNED);
        verifyNoInteractions(eventsRepository);
    }

    @Test
    @DisplayName("getProjectUpcomingEvents: 성공(hasNext=true) -> label/dDay/nextCursor 세팅")
    void getProjectUpcomingEvents_success_hasNext() {
        asProjectMember();

        LocalDate today = LocalDate.now();

        Events e1 = Events.builder()
                .projectId(projectId)
                .ownerUserId(userId)
                .eventName("공유 일정 1")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(today.plusDays(1).atStartOfDay())
                .endDate(today.plusDays(1).atStartOfDay().plusHours(1))
                .eventDescription("")
                .build();
        setEntityId(e1, "id", 1L);

        Events e2 = Events.builder()
                .projectId(projectId)
                .ownerUserId(userId)
                .eventName("휴가 1")
                .eventType(EventType.VACATION)
                .eventState(EventState.IN_PROGRESS)
                .startDate(today.plusDays(2).atStartOfDay())
                .endDate(today.plusDays(2).atStartOfDay().plusHours(1))
                .eventDescription("")
                .build();
        setEntityId(e2, "id", 2L);

        Events e3 = Events.builder()
                .projectId(projectId)
                .ownerUserId(userId)
                .eventName("개인 일정 1")
                .eventType(EventType.PRIVATE)
                .eventState(EventState.IN_PROGRESS)
                .startDate(today.plusDays(3).atStartOfDay())
                .endDate(today.plusDays(3).atStartOfDay().plusHours(1))
                .eventDescription("")
                .build();
        setEntityId(e3, "id", 3L);

        when(eventsRepository.findUpcomingVisibleEventsInProject(
                eq(projectId),
                eq(userId),
                any(LocalDateTime.class),
                isNull(),
                isNull(),
                any()
        )).thenReturn(List.of(e1, e2, e3)); // hasNext=true => page=[e1,e2]

        ProjectUpcomingEventsResponse res =
                calendarQueryService.getProjectUpcomingEvents(projectId, 2, null, null, principal);

        assertThat(res.items()).hasSize(2);
        assertThat(res.items().get(0).label()).isEqualTo("공유 일정");
        assertThat(res.items().get(1).label()).isEqualTo("휴가");

        assertThat(res.nextCursorStart()).isEqualTo(res.items().get(1).start());
        assertThat(res.nextCursorId()).isEqualTo(res.items().get(1).eventId());

        assertThat(res.items().get(0).dDay()).isEqualTo(1);
        assertThat(res.items().get(1).dDay()).isEqualTo(2);
    }
}

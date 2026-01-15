package com.moirai.alloc.calendar.query.service;

import com.moirai.alloc.calendar.command.domain.entity.EventState;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import com.moirai.alloc.calendar.command.domain.entity.Events;
import com.moirai.alloc.calendar.command.dto.response.CalendarViewResponse;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        when(principal.userId()).thenReturn(userId);
        when(squadAssignmentRepository.existsByProjectIdAndUserIdAndFinalDecision(projectId, userId, FinalDecision.ASSIGNED))
                .thenReturn(true);
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

    @Test
    @DisplayName("getCalendarView: 성공 -> 이벤트/태스크/마일스톤 통합 + 시작일시 기준 정렬")
    void getCalendarView_success_sorted() {
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

        // 일부러 역순으로 반환 -> 서비스에서 정렬되어야 함
        when(eventsRepository.findVisibleEvents(eq(projectId), any(), any(), eq(userId)))
                .thenReturn(List.of(e1, e2));

        CalendarViewResponse res = calendarQueryService.getCalendarView(projectId, from, to, "month", principal);

        assertThat(res.getItems()).hasSize(2);
        // 정렬 확인: e2(1/5) 먼저, e1(1/10) 다음
        assertThat(res.getItems().get(0).getTitle()).isEqualTo("A");
        assertThat(res.getItems().get(1).getTitle()).isEqualTo("B");

        verify(eventsRepository).findVisibleEvents(eq(projectId), any(), any(), eq(userId));
    }

    @Test
    @DisplayName("getCalendarView: from > to 이면 IllegalArgumentException")
    void getCalendarView_fail_invalidRange() {
        LocalDate from = LocalDate.of(2026, 2, 1);
        LocalDate to = LocalDate.of(2026, 1, 1);

        assertThatThrownBy(() -> calendarQueryService.getCalendarView(projectId, from, to, "month", principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from");
    }

    @Test
    @DisplayName("getCalendarView: 프로젝트 멤버가 아니면 ForbiddenException")
    void getCalendarView_fail_notMember() {
        when(squadAssignmentRepository.existsByProjectIdAndUserIdAndFinalDecision(projectId, userId, FinalDecision.ASSIGNED))
                .thenReturn(false);

        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);

        assertThatThrownBy(() -> calendarQueryService.getCalendarView(projectId, from, to, "month", principal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("프로젝트 참여자");
    }
}

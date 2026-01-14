package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.domain.entity.*;
import com.moirai.alloc.calendar.command.dto.request.*;
import com.moirai.alloc.calendar.command.dto.response.EventDetailResponse;
import com.moirai.alloc.calendar.command.dto.response.EventMemberResponse;
import com.moirai.alloc.calendar.command.dto.response.EventResponse;
import com.moirai.alloc.calendar.command.repository.EventsLogRepository;
import com.moirai.alloc.calendar.command.repository.EventsRepository;
import com.moirai.alloc.calendar.command.repository.PublicEventsMemberRepository;
import com.moirai.alloc.common.exception.ForbiddenException;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Mock private EventsRepository eventsRepository;
    @Mock private PublicEventsMemberRepository publicEventsMemberRepository;
    @Mock private EventsLogRepository eventsLogRepository;
    @Mock private SquadAssignmentRepository squadAssignmentRepository;
    @Mock private UserRepository userRepository;

    @Mock private UserPrincipal principal;

    /** saveAll(Iterable<T>) 검증용 캡처 */
    @Captor
    ArgumentCaptor<Iterable<PublicEventsMember>> pemCaptor;

    private final Long projectId = 1L;
    private final Long pmUserId = 10L;
    private final Long userId = 20L;
    private final Long eventId = 100L;

    // -----------------------
    // Helper: stub 분리 (필요한 것만 선택적으로)
    // -----------------------
    private void stubUserId(Long id) {
        when(principal.userId()).thenReturn(id);
    }

    private void stubRole(String role) {
        when(principal.role()).thenReturn(role);
    }

    private void stubProjectMember(Long principalId, boolean isMember) {
        when(squadAssignmentRepository.existsByProjectIdAndUserIdAndFinalDecision(
                eq(projectId), eq(principalId), eq(FinalDecision.ASSIGNED)
        )).thenReturn(isMember);
    }

    private void asPmMember() {
        stubUserId(pmUserId);
        stubRole("PM");                 // createSharedEvent / update* / delete* 에서 필요
        stubProjectMember(pmUserId, true);
    }

    private void asUserMemberWithRole() {
        stubUserId(userId);
        stubRole("USER");               // update* / delete* 에서 isPm 호출되므로 role 호출됨
        stubProjectMember(userId, true);
    }

    private void asUserMemberNoRole() {
        stubUserId(userId);
        stubProjectMember(userId, true); // createPersonal / createVacation / getEventDetail에 충분
    }

    // -----------------------
    // Helper: reflection setter
    // -----------------------
    private static void setField(Object target, String field, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + field, e);
        }
    }

    private static void setEntityId(Object entity, String fieldName, Long id) {
        try {
            Field f = entity.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id", e);
        }
    }

    // =========================================================
    // createSharedEvent
    // =========================================================
    @Nested
    class CreateSharedEventTests {

        @Test
        @DisplayName("성공: PM + 멤버 유효 + 기간 유효 -> Events 저장, 참여자 저장, 로그 저장")
        void createSharedEvent_success() {
            asPmMember();

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "place", "회의실 A");
            setField(req, "description", "주간 회의");
            setField(req, "memberUserIds", List.of(1L, 1L, 2L)); // 중복 포함

            when(squadAssignmentRepository.findUserIdsInProjectByDecision(eq(projectId), eq(FinalDecision.ASSIGNED), anyList()))
                    .thenAnswer(inv -> inv.getArgument(2)); // 전달된 userIds 그대로 반환

            when(eventsRepository.save(any(Events.class))).thenAnswer(inv -> {
                Events e = inv.getArgument(0);
                setEntityId(e, "id", 999L);
                return e;
            });

            EventResponse res = calendarService.createSharedEvent(projectId, req, principal);

            assertThat(res.getEventId()).isEqualTo(999L);
            assertThat(res.getOwnerUserId()).isEqualTo(pmUserId);
            assertThat(res.getEventType()).isEqualTo(EventType.PUBLIC);

            verify(publicEventsMemberRepository).saveAll(pemCaptor.capture());

            List<PublicEventsMember> savedMembers =
                    StreamSupport.stream(pemCaptor.getValue().spliterator(), false)
                            .collect(Collectors.toList());

            assertThat(savedMembers).hasSize(2);
            assertThat(savedMembers).extracting(PublicEventsMember::getUserId)
                    .containsExactlyInAnyOrder(1L, 2L);

            verify(eventsLogRepository).save(any(EventsLog.class));
        }

        @Test
        @DisplayName("실패: PM이 아니면 ForbiddenException")
        void createSharedEvent_forbidden_whenNotPm() {
            // createSharedEvent는 isPm(principal) 호출 -> role stub 필요
            stubUserId(userId);
            stubRole("USER");
            stubProjectMember(userId, true);

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", List.of(1L));

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("PM");
        }

        @Test
        @DisplayName("실패: 기간(start >= end)이면 IllegalArgumentException (principal stub 불필요)")
        void createSharedEvent_fail_whenInvalidPeriod() {
            // validatePeriod에서 바로 터지므로 principal/member stub 자체를 하지 않는다.

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", List.of(1L));

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작");
        }
    }

    // =========================================================
    // createPersonalEvent / createVacationEvent
    // =========================================================
    @Test
    @DisplayName("createPersonalEvent: 성공 -> PRIVATE 저장 + 로그 기록 (role stub 불필요)")
    void createPersonalEvent_success() {
        asUserMemberNoRole();

        PersonalEventCreateRequest req = new PersonalEventCreateRequest();
        setField(req, "eventName", "개인 일정");
        setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 9, 0));
        setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));

        when(eventsRepository.save(any(Events.class))).thenAnswer(inv -> {
            Events e = inv.getArgument(0);
            setEntityId(e, "id", 101L);
            return e;
        });

        EventResponse res = calendarService.createPersonalEvent(projectId, req, principal);

        assertThat(res.getEventId()).isEqualTo(101L);
        assertThat(res.getEventType()).isEqualTo(EventType.PRIVATE);
        verify(eventsLogRepository).save(any(EventsLog.class));
    }

    @Test
    @DisplayName("createVacationEvent: eventName blank면 기본값 '휴가' (role stub 불필요)")
    void createVacationEvent_defaultName() {
        asUserMemberNoRole();

        VacationEventCreateRequest req = new VacationEventCreateRequest();
        setField(req, "eventName", "   ");
        setField(req, "startDateTime", LocalDateTime.of(2026, 1, 11, 0, 0));
        setField(req, "endDateTime", LocalDateTime.of(2026, 1, 11, 23, 59));

        when(eventsRepository.save(any(Events.class))).thenAnswer(inv -> {
            Events e = inv.getArgument(0);
            setEntityId(e, "id", 202L);
            return e;
        });

        EventResponse res = calendarService.createVacationEvent(projectId, req, principal);
        assertThat(res.getEventName()).isEqualTo("휴가");
        assertThat(res.getEventType()).isEqualTo(EventType.VACATION);
    }

    // =========================================================
    // updateCompletion (checkEventPermission -> isPm 호출되므로 role stub 필요)
    // =========================================================
    @Test
    @DisplayName("updateCompletion: completed=true -> SUCCESS 전이 + 로그 기록")
    void updateCompletion_success() {
        asUserMemberWithRole();

        Events event = Events.builder()
                .projectId(projectId)
                .ownerUserId(userId)
                .eventName("개인 일정")
                .eventType(EventType.PRIVATE)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.of(2026, 1, 10, 9, 0))
                .endDate(LocalDateTime.of(2026, 1, 10, 10, 0))
                .eventDescription("")
                .build();
        setEntityId(event, "id", eventId);

        when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                .thenReturn(java.util.Optional.of(event));

        EventCompletionRequest req = new EventCompletionRequest();
        setField(req, "completed", true);

        EventResponse res = calendarService.updateCompletion(projectId, eventId, req, principal);

        assertThat(res.getEventState()).isEqualTo(EventState.SUCCESS);
        verify(eventsLogRepository).save(any(EventsLog.class));
    }

    // =========================================================
    // getEventDetail (현재 코드상 role stub 불필요)
    // =========================================================
    @Test
    @DisplayName("PUBLIC 상세: 참여자 중복 제거 + 이름 매핑 반환")
    void getEventDetail_public_includesMembers() {
        asUserMemberNoRole();

        Events event = Events.builder()
                .projectId(projectId)
                .ownerUserId(9999L)
                .eventName("공유 회의")
                .eventType(EventType.PUBLIC)
                .eventState(EventState.IN_PROGRESS)
                .startDate(LocalDateTime.of(2026, 1, 10, 9, 0))
                .endDate(LocalDateTime.of(2026, 1, 10, 10, 0))
                .eventDescription("")
                .build();
        setEntityId(event, "id", eventId);

        when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                .thenReturn(java.util.Optional.of(event));

        when(publicEventsMemberRepository.findByEventId(eventId)).thenReturn(List.of(
                PublicEventsMember.builder().eventId(eventId).userId(3L).build(),
                PublicEventsMember.builder().eventId(eventId).userId(3L).build(),
                PublicEventsMember.builder().eventId(eventId).userId(2L).build()
        ));

        User u3 = mock(User.class);
        when(u3.getUserId()).thenReturn(3L);
        when(u3.getUserName()).thenReturn("김철수");

        User u2 = mock(User.class);
        when(u2.getUserId()).thenReturn(2L);
        when(u2.getUserName()).thenReturn("이영희");

        when(userRepository.findAllById(List.of(3L, 2L))).thenReturn(List.of(u3, u2));

        EventDetailResponse res = calendarService.getEventDetail(projectId, eventId, principal);

        assertThat(res.getMemberUserIds()).containsExactly(3L, 2L);
        assertThat(res.getMembers()).extracting(EventMemberResponse::getUserName)
                .containsExactly("김철수", "이영희");
    }
}

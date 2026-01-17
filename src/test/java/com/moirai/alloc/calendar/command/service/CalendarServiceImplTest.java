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
import com.moirai.alloc.common.exception.NotFoundException;
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
import java.util.*;
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

    @Captor ArgumentCaptor<Iterable<PublicEventsMember>> pemCaptor;

    private final Long projectId = 1L;
    private final Long pmUserId = 10L;
    private final Long userId = 20L;
    private final Long otherUserId = 30L;
    private final Long eventId = 100L;

    // -----------------------
    // Helper: minimal stubs (STRICT_STUBS 대응)
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

    /** 멤버십만 필요한 경우(대부분): role 스텁 없음 */
    private void asMember(Long principalId) {
        stubUserId(principalId);
        stubProjectMember(principalId, true);
    }

    /** PM이 필요한 경우에만 role 스텁 */
    private void asPmMember() {
        stubUserId(pmUserId);
        stubRole("PM");
        stubProjectMember(pmUserId, true);
    }

    /** ROLE_ 접두어 분기 커버용 */
    private void asPmMemberWithRolePrefix() {
        stubUserId(pmUserId);
        stubRole("ROLE_PM");
        stubProjectMember(pmUserId, true);
    }

    // -----------------------
    // Helper: reflection setters
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

    private Events newEvent(EventType type, Long ownerId, LocalDateTime start, LocalDateTime end) {
        Events e = Events.builder()
                .projectId(projectId)
                .ownerUserId(ownerId)
                .eventName("E")
                .eventType(type)
                .eventState(EventState.IN_PROGRESS)
                .startDate(start)
                .endDate(end)
                .eventPlace("P")
                .eventDescription("")
                .build();
        setEntityId(e, "id", eventId);
        return e;
    }

    // =========================================================
    // createSharedEvent
    // =========================================================
    @Nested
    class CreateSharedEventTests {

        @Test
        @DisplayName("성공: PM + 멤버 유효 + 기간 유효 -> Events 저장, 참여자 저장(중복 제거), 로그 저장")
        void createSharedEvent_success() {
            asPmMember();

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "place", "회의실 A");
            setField(req, "description", null);
            setField(req, "memberUserIds", List.of(1L, 1L, 2L));

            when(squadAssignmentRepository.findUserIdsInProjectByDecision(eq(projectId), eq(FinalDecision.ASSIGNED), anyList()))
                    .thenAnswer(inv -> inv.getArgument(2));

            when(eventsRepository.save(any(Events.class))).thenAnswer(inv -> {
                Events e = inv.getArgument(0);
                setEntityId(e, "id", 999L);
                return e;
            });

            EventResponse res = calendarService.createSharedEvent(projectId, req, principal);

            assertThat(res.getEventId()).isEqualTo(999L);
            assertThat(res.getEventType()).isEqualTo(EventType.PUBLIC);
            assertThat(res.getOwnerUserId()).isEqualTo(pmUserId);
            assertThat(res.getDescription()).isEqualTo("");

            verify(publicEventsMemberRepository).saveAll(pemCaptor.capture());

            List<Long> savedUserIds = StreamSupport.stream(pemCaptor.getValue().spliterator(), false)
                    .map(PublicEventsMember::getUserId)
                    .collect(Collectors.toList());

            assertThat(savedUserIds).containsExactlyInAnyOrder(1L, 2L);
            verify(eventsLogRepository).save(any(EventsLog.class));
        }

        @Test
        @DisplayName("실패: PM이 아니면 ForbiddenException (role 스텁 불필요)")
        void createSharedEvent_forbidden_whenNotPm() {
            asMember(userId); // role 스텁 안 해도 isPm=false

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
        @DisplayName("실패: 기간(start >= end)이면 IllegalArgumentException (principal 스텁 불필요)")
        void createSharedEvent_fail_whenInvalidPeriod() {
            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", List.of(1L));

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 일시");
        }

        @Test
        @DisplayName("실패: memberUserIds에 null 포함 시 IllegalArgumentException (List.of는 null 불가 -> Arrays.asList 사용)")
        void createSharedEvent_fail_whenMemberContainsNull() {
            asPmMember();

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", Arrays.asList(1L, null, 2L)); // null 허용

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");

            verify(eventsRepository, never()).save(any());
            verify(publicEventsMemberRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("실패: 프로젝트 참여자가 아닌 memberUserIds 포함 시 IllegalArgumentException")
        void createSharedEvent_fail_whenMemberNotBelongToProject() {
            asPmMember();

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", List.of(1L, 2L, 3L));

            when(squadAssignmentRepository.findUserIdsInProjectByDecision(eq(projectId), eq(FinalDecision.ASSIGNED), anyList()))
                    .thenReturn(List.of(1L, 3L)); // 2L 누락

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("프로젝트 참여자가 아닌 사용자")
                    .hasMessageContaining("2");

            verify(eventsRepository, never()).save(any());
            verify(publicEventsMemberRepository, never()).saveAll(any());
        }
    }

    // =========================================================
    // createPersonalEvent / createVacationEvent
    // =========================================================
    @Test
    @DisplayName("createPersonalEvent: 성공 -> PRIVATE 저장 + 로그 기록")
    void createPersonalEvent_success() {
        asMember(userId);

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
    @DisplayName("createPersonalEvent: 프로젝트 멤버가 아니면 ForbiddenException")
    void createPersonalEvent_fail_notMember() {
        stubUserId(userId);
        stubProjectMember(userId, false);

        PersonalEventCreateRequest req = new PersonalEventCreateRequest();
        setField(req, "eventName", "개인 일정");
        setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 9, 0));
        setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));

        assertThatThrownBy(() -> calendarService.createPersonalEvent(projectId, req, principal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("프로젝트 참여자");

        verify(eventsRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVacationEvent: eventName blank면 기본값 '휴가'")
    void createVacationEvent_defaultName() {
        asMember(userId);

        VacationEventCreateRequest req = new VacationEventCreateRequest();
        setField(req, "eventName", "   ");
        setField(req, "startDateTime", LocalDateTime.of(2026, 1, 11, 0, 0));
        setField(req, "endDateTime", LocalDateTime.of(2026, 1, 11, 23, 59));
        setField(req, "description", null);

        when(eventsRepository.save(any(Events.class))).thenAnswer(inv -> {
            Events e = inv.getArgument(0);
            setEntityId(e, "id", 202L);
            return e;
        });

        EventResponse res = calendarService.createVacationEvent(projectId, req, principal);

        assertThat(res.getEventName()).isEqualTo("휴가");
        assertThat(res.getEventType()).isEqualTo(EventType.VACATION);
        assertThat(res.getDescription()).isEqualTo("");
        verify(eventsLogRepository).save(any(EventsLog.class));
    }

    // =========================================================
    // updateCompletion
    // =========================================================
    @Test
    @DisplayName("updateCompletion: completed=true -> SUCCESS 전이 + 로그 기록")
    void updateCompletion_success_completedTrue() {
        asMember(userId); // role 스텁 불필요

        Events event = newEvent(
                EventType.PRIVATE,
                userId,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );

        when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                .thenReturn(Optional.of(event));

        EventCompletionRequest req = new EventCompletionRequest();
        setField(req, "completed", true);

        EventResponse res = calendarService.updateCompletion(projectId, eventId, req, principal);

        assertThat(res.getEventState()).isEqualTo(EventState.SUCCESS);
        verify(eventsLogRepository).save(any(EventsLog.class));
    }

    @Test
    @DisplayName("updateCompletion: PRIVATE를 작성자가 아닌 사용자가 변경 시 ForbiddenException")
    void updateCompletion_fail_private_notOwner() {
        asMember(otherUserId);

        Events event = newEvent(
                EventType.PRIVATE,
                userId,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );

        when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                .thenReturn(Optional.of(event));

        EventCompletionRequest req = new EventCompletionRequest();
        setField(req, "completed", true);

        assertThatThrownBy(() -> calendarService.updateCompletion(projectId, eventId, req, principal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("개인 일정(PRIVATE)");

        verify(eventsLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCompletion: VACATION은 PM이면 작성자가 아니어도 변경 가능 (ROLE_ 접두어 분기 포함)")
    void updateCompletion_success_vacation_pm_notOwner() {
        asPmMemberWithRolePrefix();

        Events event = newEvent(
                EventType.VACATION,
                userId,
                LocalDateTime.of(2026, 1, 10, 9, 0),
                LocalDateTime.of(2026, 1, 10, 10, 0)
        );

        when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                .thenReturn(Optional.of(event));

        EventCompletionRequest req = new EventCompletionRequest();
        setField(req, "completed", true);

        EventResponse res = calendarService.updateCompletion(projectId, eventId, req, principal);

        assertThat(res.getEventState()).isEqualTo(EventState.SUCCESS);
        verify(eventsLogRepository).save(any(EventsLog.class));
    }

    // =========================================================
    // updateEvent
    // =========================================================
    @Nested
    class UpdateEventTests {

        @Test
        @DisplayName("실패: PRIVATE -> PUBLIC 변경은 PM만 가능")
        void updateEvent_fail_changeToPublic_requiresPm() {
            asMember(userId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "eventType", EventType.PUBLIC);

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("PM만");
        }

        @Test
        @DisplayName("실패: PUBLIC -> PRIVATE 타입 변경은 PM만 가능(작성자여도 불가)")
        void updateEvent_fail_changeFromPublic_requiresPm() {
            asMember(userId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "eventType", EventType.PRIVATE);

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("PM만");
        }

        @Test
        @DisplayName("실패: VACATION -> PUBLIC 변경(PM 가능)인데 memberUserIds가 null이면 IllegalArgumentException")
        void updateEvent_fail_changedToPublic_missingMembers() {
            asPmMember();

            Events event = newEvent(
                    EventType.VACATION, // PRIVATE가 아니어야 PM이 권한 체크를 통과
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "eventType", EventType.PUBLIC);
            // memberUserIds 미설정(null)

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("memberUserIds");
        }

        @Test
        @DisplayName("성공: VACATION -> PUBLIC 변경(PM) + memberUserIds 중복 제거 + 멤버 검증 + 매핑 재생성")
        void updateEvent_success_changeToPublic_withMembers_asPm() {
            asPmMember();

            Events event = newEvent(
                    EventType.VACATION, // PM 권한 통과
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            when(squadAssignmentRepository.findUserIdsInProjectByDecision(eq(projectId), eq(FinalDecision.ASSIGNED), anyList()))
                    .thenAnswer(inv -> inv.getArgument(2));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "eventType", EventType.PUBLIC);
            setField(req, "memberUserIds", List.of(1L, 1L, 2L));

            EventResponse res = calendarService.updateEvent(projectId, eventId, req, principal);

            assertThat(res.getEventType()).isEqualTo(EventType.PUBLIC);

            verify(publicEventsMemberRepository).deleteByEventId(eq(eventId));
            verify(publicEventsMemberRepository).saveAll(pemCaptor.capture());

            List<Long> savedUserIds = StreamSupport.stream(pemCaptor.getValue().spliterator(), false)
                    .map(PublicEventsMember::getUserId)
                    .collect(Collectors.toList());

            assertThat(savedUserIds).containsExactlyInAnyOrder(1L, 2L);
            verify(eventsLogRepository).save(any(EventsLog.class));
        }

        @Test
        @DisplayName("실패: memberUserIds 제공했는데 afterType != PUBLIC이면 ForbiddenException")
        void updateEvent_fail_memberUserIds_whenAfterTypeNotPublic() {
            asMember(userId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId, // owner라서 PRIVATE 권한 통과
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "memberUserIds", List.of(1L, 2L)); // 타입 변경 없이 member만

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("(PUBLIC)에서만");
        }

        @Test
        @DisplayName("실패: PUBLIC 일정에서 작성자도 PM도 아니면 checkEventPermission에서 차단")
        void updateEvent_fail_updateMembers_onPublic_notOwner_notPm() {
            asMember(otherUserId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    userId, // owner != requester
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "memberUserIds", List.of(1L, 2L));

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("작성자 또는 PM만");
        }

        @Test
        @DisplayName("실패: 이벤트 없음 -> NotFoundException (principal 스텁 불필요)")
        void updateEvent_fail_notFound() {
            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.empty());

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "eventName", "수정");

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // =========================================================
    // deleteEvent
    // =========================================================
    @Nested
    class DeleteEventTests {

        @Test
        @DisplayName("성공: PUBLIC 삭제(작성자) -> softDelete + 참여자 매핑 정리 + 로그 기록")
        void deleteEvent_success_public_owner() {
            asMember(userId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            calendarService.deleteEvent(projectId, eventId, principal);

            assertThat(event.isDeleted()).isTrue();
            verify(publicEventsMemberRepository).deleteByEventId(eq(eventId));
            verify(eventsLogRepository).save(any(EventsLog.class));
        }

        @Test
        @DisplayName("실패: 이벤트 없음 -> NotFoundException (principal 스텁 불필요)")
        void deleteEvent_fail_notFound() {
            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.deleteEvent(projectId, eventId, principal))
                    .isInstanceOf(NotFoundException.class);

            verify(eventsLogRepository, never()).save(any());
        }
    }

    // =========================================================
    // getEventDetail
    // =========================================================
    @Nested
    class GetEventDetailTests {

        @Test
        @DisplayName("PUBLIC 상세: 참여자 중복 제거 + 이름 매핑 반환")
        void getEventDetail_public_includesMembers() {
            asMember(userId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    9999L,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

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

        @Test
        @DisplayName("PRIVATE 상세: 작성자 아니면 ForbiddenException (role 스텁 불필요)")
        void getEventDetail_private_notOwner_forbidden() {
            asMember(otherUserId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId, // owner != requester
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            assertThatThrownBy(() -> calendarService.getEventDetail(projectId, eventId, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("작성자만");
        }
    }
}

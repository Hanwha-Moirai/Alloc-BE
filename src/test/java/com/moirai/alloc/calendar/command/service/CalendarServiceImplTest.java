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

    /** 프로젝트 멤버(멤버십 체크가 실제로 수행되는 케이스에서만 사용) */
    private void asProjectMember(Long principalId) {
        stubUserId(principalId);
        stubProjectMember(principalId, true);
    }

    /** 프로젝트 비멤버(Forbidden 멤버십 테스트에서만 사용) */
    private void asNotProjectMember(Long principalId) {
        stubUserId(principalId);
        stubProjectMember(principalId, false);
    }

    /** PM 멤버(해당 메서드가 PM(role) 체크를 실제로 수행하는 케이스에서만 사용) */
    private void asPmProjectMember() {
        asProjectMember(pmUserId);
        stubRole("PM");
    }

    /** ROLE_ 접두어 분기 커버용 (role 체크 수행 케이스에서만 사용) */
    private void asPmProjectMemberWithRolePrefix() {
        asProjectMember(pmUserId);
        stubRole("ROLE_PM");
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
            asPmProjectMember();

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
            // 기간 유효하므로 멤버십 체크 통과가 필요 → 멤버로 스텁
            asProjectMember(userId); // role 스텁 안 하면 isPm=false

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
        @DisplayName("실패: 기간(start >= end)이면 IllegalArgumentException (멤버십 체크 전에 기간 검증이 선행되는 경우 principal 스텁 불필요)")
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
        @DisplayName("실패: memberUserIds에 null 포함 시 IllegalArgumentException (기간 유효 → 멤버십/PM 체크 통과 필요)")
        void createSharedEvent_fail_whenMemberContainsNull() {
            // 기간이 유효한 케이스라 서비스가 멤버십 체크까지 진행함 → 멤버십/PM 스텁 필요
            asPmProjectMember();

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
            asPmProjectMember();

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
        asProjectMember(userId);

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
        asNotProjectMember(userId);

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
        asProjectMember(userId);

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
        asProjectMember(userId);

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
        asProjectMember(otherUserId);

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
        asPmProjectMemberWithRolePrefix();

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
            asProjectMember(userId);

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
            asProjectMember(userId);

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
            asPmProjectMember();

            Events event = newEvent(
                    EventType.VACATION,
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
            asPmProjectMember();

            Events event = newEvent(
                    EventType.VACATION,
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
            asProjectMember(userId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "memberUserIds", List.of(1L, 2L));

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("(PUBLIC)에서만");
        }

        @Test
        @DisplayName("실패: PUBLIC 일정에서 작성자도 PM도 아니면 checkEventPermission에서 차단")
        void updateEvent_fail_updateMembers_onPublic_notOwner_notPm() {
            asProjectMember(otherUserId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    userId,
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
            asProjectMember(userId);

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
            asProjectMember(userId);

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
            asProjectMember(otherUserId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId,
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

    // =========================================================
    // 추가 보강
    // =========================================================
    @Nested
    class AdditionalCalendarServiceImplTests {

        @Test
        @DisplayName("createSharedEvent: 프로젝트 멤버가 아니면 ForbiddenException (멤버십 체크가 role/PM 체크보다 먼저)")
        void createSharedEvent_fail_notProjectMember() {
            // 멤버십에서 바로 막히는 경로에서는 role()이 호출되지 않을 수 있으므로 role 스텁 금지
            asNotProjectMember(pmUserId);

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", List.of(1L));

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("프로젝트 참여자");

            verify(eventsRepository, never()).save(any());
            verify(publicEventsMemberRepository, never()).saveAll(any());
            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("createSharedEvent: memberUserIds가 null이면 IllegalArgumentException (기간 유효 → 멤버십/PM 체크 통과 필요)")
        void createSharedEvent_fail_memberUserIds_null() {
            // 실제 서비스 흐름상(기간 유효) 멤버십 체크가 먼저 수행되므로, 멤버십 통과를 스텁해야 IllegalArgumentException까지 도달함
            asPmProjectMember();

            SharedEventCreateRequest req = new SharedEventCreateRequest();
            setField(req, "eventName", "공유 회의");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 10, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "memberUserIds", null);

            assertThatThrownBy(() -> calendarService.createSharedEvent(projectId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("memberUserIds");

            verify(eventsRepository, never()).save(any());
            verify(publicEventsMemberRepository, never()).saveAll(any());
            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("createVacationEvent: 프로젝트 멤버가 아니면 ForbiddenException")
        void createVacationEvent_fail_notMember() {
            asNotProjectMember(userId);

            VacationEventCreateRequest req = new VacationEventCreateRequest();
            setField(req, "eventName", "휴가");
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 11, 0, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 11, 23, 59));
            setField(req, "description", "연차");

            assertThatThrownBy(() -> calendarService.createVacationEvent(projectId, req, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("프로젝트 참여자");

            verify(eventsRepository, never()).save(any());
            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateCompletion: completed=false -> IN_PROGRESS 전이 + 로그 기록")
        void updateCompletion_success_completedFalse() {
            asProjectMember(userId);

            Events event = Events.builder()
                    .projectId(projectId)
                    .ownerUserId(userId)
                    .eventName("E")
                    .eventType(EventType.PRIVATE)
                    .eventState(EventState.SUCCESS)
                    .startDate(LocalDateTime.of(2026, 1, 10, 9, 0))
                    .endDate(LocalDateTime.of(2026, 1, 10, 10, 0))
                    .eventDescription("")
                    .build();
            setEntityId(event, "id", eventId);

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventCompletionRequest req = new EventCompletionRequest();
            setField(req, "completed", false);

            EventResponse res = calendarService.updateCompletion(projectId, eventId, req, principal);

            assertThat(res.getEventState()).isEqualTo(EventState.IN_PROGRESS);
            verify(eventsLogRepository).save(any(EventsLog.class));
        }

        @Test
        @DisplayName("updateCompletion: PUBLIC에서 작성자도 PM도 아니면 ForbiddenException")
        void updateCompletion_fail_public_notOwner_notPm() {
            asProjectMember(otherUserId); // role 스텁 안함 => isPm=false

            Events event = newEvent(
                    EventType.PUBLIC,
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
                    .hasMessageContaining("작성자 또는 PM만");

            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateCompletion: 이벤트 없음 -> NotFoundException (principal 스텁 불필요)")
        void updateCompletion_fail_notFound() {
            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.empty());

            EventCompletionRequest req = new EventCompletionRequest();
            setField(req, "completed", true);

            assertThatThrownBy(() -> calendarService.updateCompletion(projectId, eventId, req, principal))
                    .isInstanceOf(NotFoundException.class);

            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateEvent: 기간 변경 시 start >= end 이면 IllegalArgumentException")
        void updateEvent_fail_invalidPeriod_onUpdate() {
            asProjectMember(userId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            EventUpdateRequest req = new EventUpdateRequest();
            setField(req, "startDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));
            setField(req, "endDateTime", LocalDateTime.of(2026, 1, 10, 11, 0));

            assertThatThrownBy(() -> calendarService.updateEvent(projectId, eventId, req, principal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("시작 일시");

            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateEvent: PUBLIC -> PRIVATE 변경(PM) 시 참여자 매핑 정리(deleteByEventId) 수행")
        void updateEvent_success_public_to_private_asPm_deletesMembers() {
            asPmProjectMember();

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

            EventResponse res = calendarService.updateEvent(projectId, eventId, req, principal);

            assertThat(res.getEventType()).isEqualTo(EventType.PRIVATE);
            verify(publicEventsMemberRepository).deleteByEventId(eq(eventId));
            verify(eventsLogRepository).save(any(EventsLog.class));
        }

        @Test
        @DisplayName("deleteEvent: PRIVATE는 작성자 아니면 ForbiddenException")
        void deleteEvent_fail_private_notOwner() {
            asProjectMember(otherUserId);

            Events event = newEvent(
                    EventType.PRIVATE,
                    userId,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            assertThatThrownBy(() -> calendarService.deleteEvent(projectId, eventId, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("작성자만");

            verify(eventsLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("getEventDetail: 프로젝트 멤버가 아니면 ForbiddenException")
        void getEventDetail_fail_notMember() {
            asNotProjectMember(userId);

            assertThatThrownBy(() -> calendarService.getEventDetail(projectId, eventId, principal))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("프로젝트 참여자");

            verify(eventsRepository, never()).findByIdAndProjectIdAndDeletedFalse(anyLong(), anyLong());
        }

        @Test
        @DisplayName("getEventDetail: 이벤트 없음 -> NotFoundException (멤버십 체크 통과 필요)")
        void getEventDetail_fail_notFound() {
            // getEventDetail은 멤버십 체크가 먼저이므로, NotFound 경로를 타려면 멤버십을 통과시켜야 함
            asProjectMember(userId);

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> calendarService.getEventDetail(projectId, eventId, principal))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("일정을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("getEventDetail: PUBLIC에서 유저 이름이 없으면 빈 문자열로 매핑")
        void getEventDetail_public_nameMissing_defaultEmpty() {
            asProjectMember(userId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    9999L,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            when(publicEventsMemberRepository.findByEventId(eventId))
                    .thenReturn(List.of(PublicEventsMember.builder().eventId(eventId).userId(1L).build()));

            when(userRepository.findAllById(anyIterable()))
                    .thenReturn(List.of()); // 1L에 해당하는 User 엔티티가 없음

            EventDetailResponse res = calendarService.getEventDetail(projectId, eventId, principal);

            assertThat(res.getMemberUserIds()).containsExactly(1L);
            assertThat(res.getMembers()).extracting(EventMemberResponse::getUserName)
                    .containsExactly("");
        }

        @Test
        @DisplayName("getEventDetail: PUBLIC인데 참여자가 없으면 userRepository 조회하지 않음")
        void getEventDetail_public_noMembers_doesNotCallUserRepository() {
            asProjectMember(userId);

            Events event = newEvent(
                    EventType.PUBLIC,
                    9999L,
                    LocalDateTime.of(2026, 1, 10, 9, 0),
                    LocalDateTime.of(2026, 1, 10, 10, 0)
            );

            when(eventsRepository.findByIdAndProjectIdAndDeletedFalse(eventId, projectId))
                    .thenReturn(Optional.of(event));

            when(publicEventsMemberRepository.findByEventId(eventId)).thenReturn(List.of());

            EventDetailResponse res = calendarService.getEventDetail(projectId, eventId, principal);

            assertThat(res.getMemberUserIds()).isEmpty();
            assertThat(res.getMembers()).isEmpty();
            verify(userRepository, never()).findAllById(anyIterable());
        }
    }
}

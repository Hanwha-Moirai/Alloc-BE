package com.moirai.alloc.notification.command.service;

import com.moirai.alloc.notification.command.domain.entity.*;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmSendLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmTemplateRepository;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.common.event.AlarmCreatedEvent;
import com.moirai.alloc.notification.common.event.AlarmUnreadChangedEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationCommandService - Stage1(Service 단위) 테스트")
class NotificationCommandServiceTest {

    @Mock AlarmLogRepository alarmLogRepository;
    @Mock AlarmTemplateRepository alarmTemplateRepository;
    @Mock AlarmSendLogRepository alarmSendLogRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks NotificationCommandService service;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 1, 16, 9, 0, 0);

    // -------------------------------------------------------------------------
    // createInternalNotifications()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createInternalNotifications() - 내부 알림 생성")
    class CreateInternalNotifications {

        @Test
        @DisplayName("성공: 템플릿 조회 → alarm_log N건 + alarm_send_log N건 생성 → AlarmCreatedEvent N회 발행 → 응답(createdCount, alarmIds) 반환")
        void success_createsLogsSendLogs_andPublishesEvents() {
            // given: template exists
            AlarmTemplate template = AlarmTemplate.builder()
                    .alarmTemplateType(AlarmTemplateType.TASK_ASSIGN)
                    .templateTitle("태스크 {{taskName}} 담당자 배정")
                    .templateContext("태스크 {{taskName}} 담당자로 지정되었습니다.")
                    .build();
            ReflectionTestUtils.setField(template, "id", 10L);

            when(alarmTemplateRepository.findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(AlarmTemplateType.TASK_ASSIGN))
                    .thenReturn(Optional.of(template));

            // given: command (2 users) - match expected assertions
            InternalNotificationCommand cmd = newCommand(
                    AlarmTemplateType.TASK_ASSIGN,
                    List.of(101L, 102L),
                    Map.of("taskName", "API 구현"),
                    TargetType.TASK,
                    555L,
                    "/tasks/555"
            );

            // alarm_log saveAll stub: assign id/createdAt like JPA did
            when(alarmLogRepository.saveAll(any()))
                    .thenAnswer(inv -> {
                        Iterable<AlarmLog> it = inv.getArgument(0);
                        List<AlarmLog> saved = new ArrayList<>();
                        long seq = 1L;
                        for (AlarmLog a : it) {
                            ReflectionTestUtils.setField(a, "id", seq++);
                            ReflectionTestUtils.setField(a, "createdAt", FIXED_NOW);
                            saved.add(a);
                        }
                        return saved;
                    });

            // alarm_send_log saveAll stub: return as-is
            when(alarmSendLogRepository.saveAll(any()))
                    .thenAnswer(inv -> {
                        Iterable<AlarmSendLog> it = inv.getArgument(0);
                        List<AlarmSendLog> saved = new ArrayList<>();
                        for (AlarmSendLog s : it) saved.add(s);
                        return saved;
                    });

            // when
            InternalNotificationCreateResponse res = service.createInternalNotifications(cmd);

            // then - response
            assertEquals(2, res.getCreatedCount());
            assertEquals(List.of(1L, 2L), res.getAlarmIds());

            // then - alarm_log 저장 내용(변수 치환 확인)
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Iterable<AlarmLog>> alarmLogCaptor = ArgumentCaptor.forClass(Iterable.class);
            verify(alarmLogRepository, times(1)).saveAll(alarmLogCaptor.capture());

            List<AlarmLog> passedLogs = toList(alarmLogCaptor.getValue());
            assertEquals(2, passedLogs.size());

            AlarmLog first = passedLogs.get(0);
            assertEquals(101L, first.getUserId());
            assertEquals(10L, first.getTemplateId());
            assertEquals("태스크 API 구현 담당자 배정", first.getAlarmTitle());
            assertEquals("태스크 API 구현 담당자로 지정되었습니다.", first.getAlarmContext());
            assertEquals(TargetType.TASK, first.getTargetType());
            assertEquals(555L, first.getTargetId());
            assertEquals("/tasks/555", first.getLinkUrl());
            assertFalse(first.isRead());
            assertFalse(first.isDeleted());

            // then - alarm_send_log 저장 내용
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Iterable<AlarmSendLog>> sendLogCaptor = ArgumentCaptor.forClass(Iterable.class);
            verify(alarmSendLogRepository, times(1)).saveAll(sendLogCaptor.capture());

            List<AlarmSendLog> passedSendLogs = toList(sendLogCaptor.getValue());
            assertEquals(2, passedSendLogs.size());

            Map<Long, AlarmSendLog> byUser = new HashMap<>();
            for (AlarmSendLog s : passedSendLogs) byUser.put(s.getUserId(), s);

            assertTrue(byUser.containsKey(101L));
            assertTrue(byUser.containsKey(102L));
            assertEquals(10L, byUser.get(101L).getTemplateId());
            assertEquals(SendLogStatus.SUCCESS, byUser.get(101L).getLogStatus());
            assertEquals("태스크 API 구현 담당자로 지정되었습니다.", byUser.get(101L).getTemplateContext());

            // then - 이벤트 publish (AlarmCreatedEvent 2번)
            verify(eventPublisher, times(2)).publishEvent(isA(AlarmCreatedEvent.class));

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

            List<AlarmCreatedEvent> createdEvents = eventCaptor.getAllValues().stream()
                    .filter(e -> e instanceof AlarmCreatedEvent)
                    .map(e -> (AlarmCreatedEvent) e)
                    .toList();

            assertEquals(2, createdEvents.size());
            assertEquals(Set.of(101L, 102L),
                    new HashSet<>(createdEvents.stream().map(AlarmCreatedEvent::userId).toList()));
            assertEquals(Set.of(1L, 2L),
                    new HashSet<>(createdEvents.stream().map(AlarmCreatedEvent::alarmId).toList()));
            assertEquals("태스크 API 구현 담당자 배정", createdEvents.get(0).title());
            assertEquals("태스크 API 구현 담당자로 지정되었습니다.", createdEvents.get(0).content());
        }

        @Test
        @DisplayName("실패: 템플릿이 없으면 404 예외(ResponseStatusException) + 저장/이벤트 발행 없음")
        void templateNotFound_throws404() {
            // given
            when(alarmTemplateRepository.findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(any()))
                    .thenReturn(Optional.empty());

            InternalNotificationCommand cmd = newCommand(
                    AlarmTemplateType.TASK_ASSIGN,
                    List.of(101L, 102L),
                    Map.of("taskName", "API 구현"),
                    TargetType.TASK,
                    555L,
                    "/tasks/555"
            );

            // when
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.createInternalNotifications(cmd));

            // then
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verifyNoInteractions(alarmLogRepository);
            verifyNoInteractions(alarmSendLogRepository);
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // -------------------------------------------------------------------------
    // markRead()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("markRead() - 단건 읽음 처리")
    class MarkRead {

        @Test
        @DisplayName("성공: 업데이트 1건 이상이면 UNREAD 변경 이벤트 발행")
        void success_publishesUnreadChangedEvent() {
            when(alarmLogRepository.markRead(1L, 10L)).thenReturn(1);

            assertDoesNotThrow(() -> service.markRead(1L, 10L));

            verify(alarmLogRepository, times(1)).markRead(1L, 10L);
            verify(eventPublisher, times(1)).publishEvent(isA(AlarmUnreadChangedEvent.class));
        }

        @Test
        @DisplayName("실패: 업데이트 0건이면 404 예외 + 이벤트 발행 없음")
        void notFound_throws404_andDoesNotPublishEvent() {
            when(alarmLogRepository.markRead(1L, 10L)).thenReturn(0);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.markRead(1L, 10L));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // -------------------------------------------------------------------------
    // markAllRead()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("markAllRead() - 전체 읽음 처리")
    class MarkAllRead {

        @Test
        @DisplayName("성공: bulk update 실행 후 UNREAD 변경 이벤트 발행")
        void publishesUnreadChangedEvent() {
            when(alarmLogRepository.markAllRead(1L)).thenReturn(3);

            service.markAllRead(1L);

            verify(alarmLogRepository, times(1)).markAllRead(1L);
            verify(eventPublisher, times(1)).publishEvent(isA(AlarmUnreadChangedEvent.class));
        }
    }

    // -------------------------------------------------------------------------
    // deleteNotification()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteNotification() - 단건 삭제(Soft Delete)")
    class DeleteNotification {

        @Test
        @DisplayName("성공: 업데이트 1건 이상이면 UNREAD 변경 이벤트 발행")
        void success_publishesUnreadChangedEvent() {
            when(alarmLogRepository.softDeleteOne(1L, 10L)).thenReturn(1);

            service.deleteNotification(1L, 10L);

            verify(alarmLogRepository, times(1)).softDeleteOne(1L, 10L);
            verify(eventPublisher, times(1)).publishEvent(isA(AlarmUnreadChangedEvent.class));
        }

        @Test
        @DisplayName("실패: 업데이트 0건이면 404 예외 + 이벤트 발행 없음")
        void notFound_throws404_andDoesNotPublishEvent() {
            when(alarmLogRepository.softDeleteOne(1L, 10L)).thenReturn(0);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.deleteNotification(1L, 10L));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteAllRead()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteAllRead() - 읽은 알림 전체 삭제(Soft Delete)")
    class DeleteAllRead {

        @Test
        @DisplayName("성공: bulk delete 실행 후 UNREAD 변경 이벤트 발행")
        void publishesUnreadChangedEvent() {
            when(alarmLogRepository.softDeleteAllRead(1L)).thenReturn(5);

            service.deleteAllRead(1L);

            verify(alarmLogRepository, times(1)).softDeleteAllRead(1L);
            verify(eventPublisher, times(1)).publishEvent(isA(AlarmUnreadChangedEvent.class));
        }
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------
    private static InternalNotificationCommand newCommand(
            AlarmTemplateType templateType,
            List<Long> targetUserIds,
            Map<String, String> variables,
            TargetType targetType,
            Long targetId,
            String linkUrl
    ) {
        return InternalNotificationCommand.builder()
                .templateType(templateType)
                .targetUserIds(targetUserIds)
                .variables(variables)
                .targetType(targetType)
                .targetId(targetId)
                .linkUrl(linkUrl)
                .build();
    }

    private static <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        for (T t : it) list.add(t);
        return list;
    }
}

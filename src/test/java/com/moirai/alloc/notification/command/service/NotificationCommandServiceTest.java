package com.moirai.alloc.notification.command.service;

import com.moirai.alloc.notification.command.domain.entity.*;
import com.moirai.alloc.notification.common.contract.AlarmTemplateType;
import com.moirai.alloc.notification.common.contract.InternalNotificationCommand;
import com.moirai.alloc.notification.common.contract.InternalNotificationCreateResponse;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmSendLogRepository;
import com.moirai.alloc.notification.command.repository.AlarmTemplateRepository;
import com.moirai.alloc.notification.common.contract.TargetType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @InjectMocks NotificationCommandService service;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 1, 16, 9, 0, 0);

    // -------------------------------------------------------------------------
    // createInternalNotifications()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createInternalNotifications() - 내부 알림 생성")
    class CreateInternalNotifications {

        @Test
        @DisplayName("성공: 템플릿 조회 → alarm_log N건 + alarm_send_log N건 생성 → 응답(createdCount, alarmIds) 반환")
        void success_createsLogsSendLogs() {
            // given
            AlarmTemplate template = AlarmTemplate.builder()
                    .alarmTemplateType(AlarmTemplateType.TASK_ASSIGN)
                    .templateTitle("태스크 {{taskName}} 담당자 배정")
                    .templateContext("태스크 {{taskName}} 담당자로 지정되었습니다.")
                    .build();
            ReflectionTestUtils.setField(template, "id", 10L);

            when(alarmTemplateRepository.findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(AlarmTemplateType.TASK_ASSIGN))
                    .thenReturn(Optional.of(template));

            InternalNotificationCommand cmd = newCommand(
                    AlarmTemplateType.TASK_ASSIGN,
                    List.of(101L, 102L),
                    Map.of("taskName", "API 구현"),
                    TargetType.TASK,
                    555L,
                    "/tasks/555"
            );

            // alarm_log saveAll 스텁: 들어온 엔티티에 id/createdAt을 세팅해 저장된 것처럼 반환
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
            assertEquals("태스크 API 구현 담당자 배정", passedLogs.get(0).getAlarmTitle());
            assertEquals("태스크 API 구현 담당자로 지정되었습니다.", passedLogs.get(0).getAlarmContext());
            assertEquals(TargetType.TASK, passedLogs.get(0).getTargetType());
            assertEquals(555L, passedLogs.get(0).getTargetId());
            assertEquals("/tasks/555", passedLogs.get(0).getLinkUrl());
            assertFalse(passedLogs.get(0).isRead());
            assertFalse(passedLogs.get(0).isDeleted());

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
        }

        @Test
        @DisplayName("실패: 템플릿이 없으면 404 예외(ResponseStatusException) + 저장 없음")
        void templateNotFound_throws404() {
            // given
            when(alarmTemplateRepository.findTopByAlarmTemplateTypeAndDeletedFalseOrderByIdDesc(any()))
                    .thenReturn(Optional.empty());

            InternalNotificationCommand cmd = newCommand(
                    AlarmTemplateType.TASK_ASSIGN,
                    List.of(1L),
                    Map.of("taskName", "X"),
                    TargetType.TASK,
                    10L,
                    null
            );

            // when
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.createInternalNotifications(cmd));

            // then
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            verifyNoInteractions(alarmLogRepository);
            verifyNoInteractions(alarmSendLogRepository);
        }
    }

    // -------------------------------------------------------------------------
    // markRead()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("markRead() - 단건 읽음 처리")
    class MarkRead {

        @Test
        @DisplayName("성공: 업데이트 1건 이상이면 정상 처리")
        void success_updates() {
            when(alarmLogRepository.markRead(1L, 10L)).thenReturn(1);

            assertDoesNotThrow(() -> service.markRead(1L, 10L));

            verify(alarmLogRepository, times(1)).markRead(1L, 10L);
        }

        @Test
        @DisplayName("실패: 업데이트 0건이면 404 예외")
        void notFound_throws404() {
            when(alarmLogRepository.markRead(1L, 10L)).thenReturn(0);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.markRead(1L, 10L));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    // -------------------------------------------------------------------------
    // markAllRead()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("markAllRead() - 전체 읽음 처리")
    class MarkAllRead {

        @Test
        @DisplayName("성공: bulk update 실행")
        void updatesAllRead() {
            when(alarmLogRepository.markAllRead(1L)).thenReturn(3);

            service.markAllRead(1L);

            verify(alarmLogRepository, times(1)).markAllRead(1L);
        }
    }

    // -------------------------------------------------------------------------
    // deleteNotification()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteNotification() - 단건 삭제(Soft Delete)")
    class DeleteNotification {

        @Test
        @DisplayName("성공: 업데이트 1건 이상이면 정상 처리")
        void success_updates() {
            when(alarmLogRepository.softDeleteOne(1L, 10L)).thenReturn(1);

            service.deleteNotification(1L, 10L);

            verify(alarmLogRepository, times(1)).softDeleteOne(1L, 10L);
        }

        @Test
        @DisplayName("실패: 업데이트 0건이면 404 예외")
        void notFound_throws404() {
            when(alarmLogRepository.softDeleteOne(1L, 10L)).thenReturn(0);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> service.deleteNotification(1L, 10L));

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    // -------------------------------------------------------------------------
    // deleteAllRead()
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteAllRead() - 읽은 알림 전체 삭제(Soft Delete)")
    class DeleteAllRead {

        @Test
        @DisplayName("성공: bulk delete 실행")
        void deletesAllRead() {
            when(alarmLogRepository.softDeleteAllRead(1L)).thenReturn(5);

            service.deleteAllRead(1L);

            verify(alarmLogRepository, times(1)).softDeleteAllRead(1L);
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

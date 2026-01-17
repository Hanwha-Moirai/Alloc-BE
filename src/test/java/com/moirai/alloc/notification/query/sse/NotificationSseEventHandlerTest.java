// src/test/java/com/moirai/alloc/notification/query/sse/NotificationSseEventHandlerTest.java
package com.moirai.alloc.notification.query.sse;

import com.moirai.alloc.notification.command.domain.entity.TargetType;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.common.event.AlarmCreatedEvent;
import com.moirai.alloc.notification.common.event.AlarmUnreadChangedEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSseEventHandlerTest {

    @Mock
    NotificationSseEmitters emitters;

    @Mock
    AlarmLogRepository alarmLogRepository;

    @InjectMocks
    NotificationSseEventHandler handler;

    @Nested
    @DisplayName("onAlarmCreated()")
    class OnAlarmCreated {

        @Test
        @DisplayName("알림 생성 이벤트 수신 시 NOTIFICATION 전송 후 UNREAD_COUNT를 조회/전송한다")
        void sendsNotificationAndUnreadCount() {
            // given
            long userId = 10L;
            long unread = 3L;

            AlarmCreatedEvent event = AlarmCreatedEvent.builder()
                    .userId(userId)
                    .alarmId(1000L)
                    .title("title")
                    .content("content")
                    .targetType(TargetType.TASK)
                    .targetId(55L)
                    .linkUrl("http://link")
                    .createdAt(LocalDateTime.of(2026, 1, 15, 12, 0))
                    .build();

            when(alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId)).thenReturn(unread);

            // when
            handler.onAlarmCreated(event);

            // then
            InOrder inOrder = inOrder(emitters, alarmLogRepository);
            inOrder.verify(emitters).sendToUser(userId, "NOTIFICATION", event);
            inOrder.verify(alarmLogRepository).countByUserIdAndReadFalseAndDeletedFalse(userId);
            inOrder.verify(emitters).sendToUser(userId, "UNREAD_COUNT", unread);

            verifyNoMoreInteractions(emitters, alarmLogRepository);
        }
    }

    @Nested
    @DisplayName("onUnreadChanged()")
    class OnUnreadChanged {

        @Test
        @DisplayName("미읽음 변경 이벤트 수신 시 UNREAD_COUNT를 조회/전송한다")
        void sendsUnreadCountOnly() {
            // given
            long userId = 20L;
            long unread = 7L;

            AlarmUnreadChangedEvent event = AlarmUnreadChangedEvent.builder()
                    .userId(userId)
                    .build();

            when(alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId)).thenReturn(unread);

            // when
            handler.onUnreadChanged(event);

            // then
            InOrder inOrder = inOrder(alarmLogRepository, emitters);
            inOrder.verify(alarmLogRepository).countByUserIdAndReadFalseAndDeletedFalse(userId);
            inOrder.verify(emitters).sendToUser(userId, "UNREAD_COUNT", unread);

            verifyNoMoreInteractions(emitters, alarmLogRepository);
        }
    }
}

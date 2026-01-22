package com.moirai.alloc.notification.query.service;

import com.moirai.alloc.notification.command.domain.entity.AlarmLog;
import com.moirai.alloc.notification.common.contract.TargetType;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.query.dto.response.NotificationPageResponse;
import com.moirai.alloc.notification.query.dto.response.NotificationSummaryResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock
    AlarmLogRepository alarmLogRepository;

    @InjectMocks
    NotificationQueryService service;

    @Nested
    @DisplayName("getMyNotifications()")
    class GetMyNotifications {

        @Test
        @DisplayName("size가 null이면 기본 page size(10)를 적용하고, createdAt DESC 정렬로 조회한다")
        void defaultSize_whenSizeIsNull() {
            // given
            long userId = 1L;

            AlarmLog a1 = alarmLog(101L, userId, "t1", "c1", LocalDateTime.of(2026, 1, 10, 10, 0));
            AlarmLog a2 = alarmLog(102L, userId, "t2", "c2", LocalDateTime.of(2026, 1, 11, 10, 0));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            // repository가 어떤 pageable로 호출되든 Page를 반환하도록 설정
            when(alarmLogRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(a2, a1), PageRequest.of(0, 10), 2));

            // when
            NotificationPageResponse res = service.getMyNotifications(userId, 0, null);

            // then: repository 호출 pageable 검증
            verify(alarmLogRepository).findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(10);

            Sort.Order createdAtOrder = pageable.getSort().getOrderFor("createdAt");
            assertThat(createdAtOrder).isNotNull();
            assertThat(createdAtOrder.getDirection()).isEqualTo(Sort.Direction.DESC);

            // then: 응답 매핑 검증
            assertThat(res.getNotifications()).hasSize(2);

            NotificationSummaryResponse first = res.getNotifications().get(0);
            assertThat(first.getNotificationId()).isEqualTo(102L);
            assertThat(first.getTitle()).isEqualTo("t2");
            assertThat(first.getContent()).isEqualTo("c2");
            assertThat(first.isRead()).isFalse();
            assertThat(first.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 11, 10, 0));
            assertThat(first.getTargetType()).isEqualTo(TargetType.TASK);
            assertThat(first.getTargetId()).isEqualTo(999L);
            assertThat(first.getLinkUrl()).isEqualTo("http://link");

            assertThat(res.getPagination().getCurrentPage()).isEqualTo(0);
            assertThat(res.getPagination().getTotalPages()).isEqualTo(1);
            assertThat(res.getPagination().getTotalItems()).isEqualTo(2);
        }

        @Test
        @DisplayName("page가 음수이면 0으로 보정한다")
        void pageIsClampedToZero_whenPageIsNegative() {
            // given
            long userId = 1L;

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            when(alarmLogRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            // when
            service.getMyNotifications(userId, -10, 20);

            // then
            verify(alarmLogRepository).findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("size가 0 이하이면 기본 page size(10)를 적용한다")
        void defaultSize_whenSizeIsZeroOrNegative() {
            // given
            long userId = 1L;
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            when(alarmLogRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            // when
            service.getMyNotifications(userId, 0, 0);

            // then
            verify(alarmLogRepository).findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("Pagination(current/totalPages/totalItems)을 Page 결과에서 정확히 매핑한다")
        void mapsPaginationCorrectly() {
            // given
            long userId = 1L;

            PageRequest pageable = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
            long totalElements = 21L; // size=10, page=1이면 totalPages=3 기대
            AlarmLog a = alarmLog(201L, userId, "t", "c", LocalDateTime.of(2026, 1, 12, 9, 0));

            when(alarmLogRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(a), pageable, totalElements));

            // when
            NotificationPageResponse res = service.getMyNotifications(userId, 1, 10);

            // then
            assertThat(res.getPagination().getCurrentPage()).isEqualTo(1);
            assertThat(res.getPagination().getTotalPages()).isEqualTo(3);
            assertThat(res.getPagination().getTotalItems()).isEqualTo(21);
        }
    }

    @Nested
    @DisplayName("getMyUnreadCount()")
    class GetMyUnreadCount {

        @Test
        @DisplayName("Repository countByUserIdAndReadFalseAndDeletedFalse 결과를 그대로 반환한다")
        void delegatesToRepository() {
            // given
            long userId = 7L;
            when(alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId)).thenReturn(5L);

            // when
            long unread = service.getMyUnreadCount(userId);

            // then
            assertThat(unread).isEqualTo(5L);
            verify(alarmLogRepository).countByUserIdAndReadFalseAndDeletedFalse(userId);
        }
    }

    /**
     * AlarmLog는 JPA/Auditing으로 id/createdAt이 세팅되는 전제라서,
     * Stage 1(단위 테스트)에서는 ReflectionTestUtils로 필요한 필드를 주입한다.
     *
     */
    private static AlarmLog alarmLog(Long alarmId, Long userId, String title, String content, LocalDateTime createdAt) {
        AlarmLog alarm = AlarmLog.builder()
                .userId(userId)
                .templateId(1L)
                .alarmTitle(title)
                .alarmContext(content)
                .targetType(TargetType.TASK)
                .targetId(999L)
                .linkUrl("http://link")
                .build();

        ReflectionTestUtils.setField(alarm, "id", alarmId);
        ReflectionTestUtils.setField(alarm, "createdAt", createdAt); // BaseTimeEntity 필드명 전제
        return alarm;
    }
}

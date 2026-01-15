package com.moirai.alloc.notification.query.service;

import com.moirai.alloc.common.dto.pagination.Pagination;
import com.moirai.alloc.notification.command.domain.entity.AlarmLog;
import com.moirai.alloc.notification.command.repository.AlarmLogRepository;
import com.moirai.alloc.notification.query.dto.response.NotificationPageResponse;
import com.moirai.alloc.notification.query.dto.response.NotificationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final AlarmLogRepository alarmLogRepository;

    /**
     * 내 알림 목록 조회(페이징)
     * - deleted=false만 조회(soft delete 반영)
     * - createdAt DESC 정렬
     * - 반환: notifications + pagination(current/totalPages/totalItems)
     */
    public NotificationPageResponse getMyNotifications(Long userId, int page, Integer size) {
        int pageSize = resolveSize(size);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<AlarmLog> result =
                alarmLogRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable);

        List<NotificationSummaryResponse> items = result.getContent().stream()
                .map(NotificationSummaryResponse::from)
                .toList();

        Pagination pagination = Pagination.builder()
                .currentPage(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalItems(result.getTotalElements())
                .build();

        return NotificationPageResponse.builder()
                .notifications(items)
                .pagination(pagination)
                .build();
    }

    /**
     * 내 미읽음 알림 개수 조회
     * - read=false AND deleted=false
     * - UI 뱃지/헤더 카운트 등에 사용
     */
    public long getMyUnreadCount(Long userId) {
        return alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    /**
     * page size 방어(미지정/0 이하 → 기본값)
     */
    private int resolveSize(Integer size) {
        return (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    }
}

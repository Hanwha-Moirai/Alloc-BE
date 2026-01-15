package com.moirai.alloc.notification.query.service;

import com.moirai.alloc.common.dto.Pagination;
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

    public NotificationPageResponse getMyNotifications(Long userId, int page, Integer size) {
        int pageSize = resolveSize(size);
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<com.moirai.alloc.notification.command.domain.entity.AlarmLog> result =
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

    public long getMyUnreadCount(Long userId) {
        return alarmLogRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    private int resolveSize(Integer size) {
        return (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    }
}

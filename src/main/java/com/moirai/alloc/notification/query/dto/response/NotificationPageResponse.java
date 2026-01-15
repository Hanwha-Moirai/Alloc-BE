package com.moirai.alloc.notification.query.dto.response;

import com.moirai.alloc.common.dto.pagination.Pagination;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationPageResponse {
    private final List<NotificationSummaryResponse> notifications;
    private final Pagination pagination;
}

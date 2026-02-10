package com.moirai.alloc.calendar.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moirai.alloc.calendar.command.domain.entity.EventType;
import java.time.LocalDateTime;

public record ProjectUpcomingEventItemResponse(
        Long eventId,
        EventType eventType,
        String label,          // "공유 일정" | "개인 일정" | "휴가"
        String title,
        LocalDateTime start,
        LocalDateTime end,
        long dDay              // start 기준 D-day
) {
    /**
     * 하위 호환용 별칭: 기존 클라이언트의 startDate/endDate 바인딩 지원.
     */
    @JsonProperty("startDate")
    public LocalDateTime startDate() {
        return start;
    }

    @JsonProperty("endDate")
    public LocalDateTime endDate() {
        return end;
    }
}

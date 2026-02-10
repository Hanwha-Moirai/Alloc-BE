package com.moirai.alloc.calendar.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record WeeklyEventCountResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        long total
) {
    /**
     * 하위 호환용 별칭: 기존 프론트에서 count 필드를 읽는 경우를 지원.
     */
    @JsonProperty("count")
    public long count() {
        return total;
    }
}

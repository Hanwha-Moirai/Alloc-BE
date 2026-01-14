package com.moirai.alloc.calendar.command.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventMemberResponse {
    private Long userId;
    private String userName;

    public static EventMemberResponse of(Long userId, String userName) {
        return EventMemberResponse.builder()
                .userId(userId)
                .userName(userName)
                .build();
    }
}

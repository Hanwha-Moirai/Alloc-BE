package com.moirai.alloc.admin.query.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminJobListItem {

    private final Long jobId;

    private final String jobName;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;
}

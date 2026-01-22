package com.moirai.alloc.admin.query.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminTechStackListItem {


    private final Long techId;

    private final String techName;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;
}

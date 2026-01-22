package com.moirai.alloc.admin.query.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminTitleStandardListItem {

    private Long titleStandardId;
    private String titleName;
    private Integer monthlyCost;

    private LocalDateTime updatedAt;
}

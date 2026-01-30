package com.moirai.alloc.home.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeProjectSummaryDTO {
    private int activeCount;
    private int delayedCount;
    private int closedCount;
}

package com.moirai.alloc.management.query.dto.candidateList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JobAssignmentSummaryDTO {
    private Long jobId;
    private String jobName;
    private int selectedCount;
    private int requiredCount;
    private Status status;

    public enum Status {
        NONE,        // 미선택
        INCOMPLETE,  // 미충족
        COMPLETE     // 충족
    }
}

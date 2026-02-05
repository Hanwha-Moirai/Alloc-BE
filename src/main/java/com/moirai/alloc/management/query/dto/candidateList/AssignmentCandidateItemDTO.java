package com.moirai.alloc.management.query.dto.candidateList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignmentCandidateItemDTO {
    private Long userId;
    private String userName;

    private Long jobId; // 도메인 식별 용도
    private String jobName;
    private String mainSkill;

    private Integer monthlyWage;
    private WorkStatus workStatus;

    private int skillScore;
    private int experienceScore;
    private int availabilityScore;

    private boolean selected;

    public enum WorkStatus {
        AVAILABLE,  // 대기중
        ASSIGNED    // 투입중
    }
}

package com.moirai.alloc.management.query.dto.candidate_list;

import com.moirai.alloc.management.query.view.WorkStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignmentCandidateItemDTO {
    private Long userId;
    private String userName;

    private String jobName;
    private String mainSkill;

    private Integer monthlyWage;
    private WorkStatus workStatus;
    private Integer fitnessScore;
    private boolean selected;
}

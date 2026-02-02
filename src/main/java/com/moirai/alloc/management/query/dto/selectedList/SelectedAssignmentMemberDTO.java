package com.moirai.alloc.management.query.dto.selectedList;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SelectedAssignmentMemberDTO {

    private Long assignmentId;
    private Long userId;
    private String userName;
    private String jobName;
    private String mainSkill;

    private Integer monthlyWage;

    private AssignmentStatus assignmentStatus;
    private FinalDecision finalDecision;

    private int skillScore;
    private int experienceScore;
    private int availabilityScore;
    private int finalScore;

}

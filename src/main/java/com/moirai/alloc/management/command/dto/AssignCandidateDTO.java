package com.moirai.alloc.management.command.dto;

import lombok.Getter;

import java.util.List;
@Getter
public class AssignCandidateDTO {
    private Long projectId;
    private List<JobAssignmentDTO> assignments;

    public AssignCandidateDTO(
            Long projectId,
            List<JobAssignmentDTO> assignments
    ) {
        this.projectId = projectId;
        this.assignments = assignments;
    }

}
//어떤 프로젝트 단위별로, 추천된 결과 묶음
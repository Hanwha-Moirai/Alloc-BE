package com.moirai.alloc.management.command.dto;

import lombok.Getter;

import java.util.List;
@Getter
public class AssignCandidateDTO {
    private Long projectId;
    private List<JobAssignmentDTO> assignments;


}
//어떤 프로젝트, 직군별로 누가 선택되었는지 dto
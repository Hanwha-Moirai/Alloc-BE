package com.moirai.alloc.management.query.dto.userAssign;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPendingAssignmentDTO {

    private Long assignmentId;
    private Long projectId;
    private String projectName;

    private AssignmentStatus status; // REQUESTED 고정이지만 명시
}
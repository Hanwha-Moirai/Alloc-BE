package com.moirai.alloc.management.query.dto.selectedWorker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssignedMemberDTO {
    private Long userId;
    private String employeeName;
    private String jobName;
    private Long projectId;
}

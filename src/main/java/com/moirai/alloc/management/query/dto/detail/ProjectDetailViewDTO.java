package com.moirai.alloc.management.query.dto.detail;

import com.moirai.alloc.project.command.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectDetailViewDTO {
    private Long projectId;
    private String projectName;
    private Project.ProjectStatus status;
    private Project.ProjectType projectType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String partners;
    private String description;
    private Integer predictedCost;

    // 타 팀원
    //progressRate;
    //RISK
    //ACTIVITY
    // SCHEDULE
}

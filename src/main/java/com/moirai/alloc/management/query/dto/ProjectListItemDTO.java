package com.moirai.alloc.management.query.dto;

import com.moirai.alloc.project.command.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectListItemDTO {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Project.ProjectStatus status;

    // 다른 팀원 담당
    private Integer progressRate;      // 진행률 (%)
    private String documentStatus;      // DRAFT / SENT
    private String riskLevel;           // HIGH / MEDIUM / LOW

}

package com.moirai.alloc.management.command.dto;

import com.moirai.alloc.project.command.domain.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProjectSpecParseResponse {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer predictedCost;
    private String partners;
    private String description;
    private Project.ProjectType projectType;
    private Project.ProjectStatus projectStatus;
    private int pageCount;
}

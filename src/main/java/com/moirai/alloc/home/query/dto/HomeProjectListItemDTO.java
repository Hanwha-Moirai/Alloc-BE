package com.moirai.alloc.home.query.dto;

import com.moirai.alloc.project.command.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class HomeProjectListItemDTO {

    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Project.ProjectStatus status;

    private Integer progressRate;
}

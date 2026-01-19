package com.moirai.alloc.management.command.dto;

import com.moirai.alloc.project.command.domain.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
public class EditProjectDTO {
    private Long projectId;
    private String projectName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer predictedCost;
    private Project.ProjectType projectType;
    private Project.ProjectStatus projectStatus;
    private String partners;
    private String description;

}

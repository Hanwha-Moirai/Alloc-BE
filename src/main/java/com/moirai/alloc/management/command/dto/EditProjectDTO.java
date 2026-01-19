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
    @NotNull
    private Long projectId;
    @NotBlank
    private String projectName;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Integer predictedCost;
    @NotNull
    private Project.ProjectType projectType;
    @NotNull
    private Project.ProjectStatus projectStatus;

    private String partners;
    private String description;

}

package com.moirai.alloc.management.command.dto;

import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.domain.vo.TechRequirement;
import com.moirai.alloc.project.command.domain.Project;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
@Getter
public class RegisterProjectCommandDTO {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String partners;

    @NotNull
    private Integer predictedCost;

    @NotNull
    private Project.ProjectType projectType;

    private String description;

    @NotEmpty
    @Valid
    private List<JobRequirement> jobRequirements;

    @NotEmpty
    @Valid
    private List<TechRequirement> techRequirements;
}

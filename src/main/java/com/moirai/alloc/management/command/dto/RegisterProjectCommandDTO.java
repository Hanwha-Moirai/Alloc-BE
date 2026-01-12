package com.moirai.alloc.management.command.dto;

import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.domain.vo.TechRequirement;
import com.moirai.alloc.project.command.domain.Project;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
@Getter
public class RegisterProjectCommandDTO {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String partners;
    private Integer predictedCost;
    private Project.ProjectType projectType;
    private String description;

    private List<JobRequirement> jobRequirements;
    private List<TechRequirement> techRequirements;
}

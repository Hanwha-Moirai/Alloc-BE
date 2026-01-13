package com.moirai.alloc.management.query.dto.registration;

import com.moirai.alloc.management.domain.entity.TechReqLevel;
import com.moirai.alloc.project.command.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
@Getter
@AllArgsConstructor
public class ProjectRegistrationViewDTO {
    private List<JobOptionDTO> jobOptions;
    private List<TechOptionDTO> techOptions;

    private List<Project.ProjectType> projectTypes;
    private List<TechReqLevel> techLevels;
}

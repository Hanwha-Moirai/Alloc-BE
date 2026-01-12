package com.moirai.alloc.management.query.dto.registration;

import com.moirai.alloc.management.domain.entity.TechReqLevel;
import com.moirai.alloc.project.command.domain.Project;
import java.util.List;

public class ProjectRegistrationViewDTO {
    private List<JobOptionDTO> jobOptions;
    private List<TechOptionDTO> techOptions;

    private List<Project.ProjectType> projectTypes;
    private List<TechReqLevel> techLevels;
}

package com.moirai.alloc.management.command;
import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.domain.vo.TechRequirement;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RegisterProject {
    private final ProjectRepository projectRepository;
    public RegisterProject(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    public Long registerProject(RegisterProjectCommandDTO command) {
//        1) project 내용을 입력한다.
//        2) project를 저장한다.

        Project project = Project.builder()
                .name(command.getName())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .partners(command.getPartners())
                .predictedCost(command.getPredictedCost())
                .projectType(command.getProjectType())
                .description(command.getDescription())
                .build();

        List<JobRequirement> jobReqs =
                command.getJobRequirements().stream()
                        .map(req ->
                                new JobRequirement(
                                        req.getJobId(),
                                        req.getRequiredCount()
                                )
                        )
                        .toList();

        project.changeJobRequirements(jobReqs);

        List<TechRequirement> techReqs =
                command.getTechRequirements().stream()
                        .map(req ->
                                new TechRequirement(
                                        req.getTechId(),
                                        req.getTechLevel()
                                )
                        )
                        .toList();

        project.changeTechRequirements(techReqs);

        return projectRepository.save(project).getProjectId();

    }
}

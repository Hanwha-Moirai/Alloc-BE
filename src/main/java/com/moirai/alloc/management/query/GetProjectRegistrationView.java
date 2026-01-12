package com.moirai.alloc.management.query;

import com.moirai.alloc.management.domain.entity.TechReqLevel;
import com.moirai.alloc.management.query.dto.JobOptionDTO;
import com.moirai.alloc.management.query.dto.TechOptionDTO;
import com.moirai.alloc.project.command.domain.Project;

import java.util.List;

public class GetProjectRegistrationView {

    /*private final JobStandardRepostiory jobStandardRepostiory;
    private final TechStandardRepository techStandardRepository;

    public GetProjectRegistrationView(
            JobStandardRepository jobStandardRepository,
            TechStandardRepository techStandardRepository
    ) {
        this.jobStandardRepository = jobStandardRepository;
        this.techStandardRepository = techStandardRepository;
    }
    public ProjectRegistrationView getView() {

        List<JobOptionDTO> jobs =
                jobStandardRepository.findAll().stream()
                        .map(job -> new JobOptionDTO(
                                job.getJobId(),
                                job.getJobName()
                        ))
                        .toList();

        List<TechOptionDTO> techs =
                techStandardRepository.findAll().stream()
                        .map(tech -> new TechOptionDTO(
                                tech.getTechId(),
                                tech.getTechName()
                        ))
                        .toList();

        return new ProjectRegistrationView(
                jobs,
                techs,
                List.of(Project.ProjectType.values()),
                List.of(TechReqLevel.values())
        );
    }*/
}

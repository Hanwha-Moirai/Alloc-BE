package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.JobStandardRepository;
import com.moirai.alloc.management.TechStandardRepository;
import com.moirai.alloc.management.domain.entity.TechReqLevel;
import com.moirai.alloc.management.query.dto.registration.JobOptionDTO;
import com.moirai.alloc.management.query.dto.registration.ProjectRegistrationViewDTO;
import com.moirai.alloc.management.query.dto.registration.TechOptionDTO;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetProjectRegistrationView {

    private final JobStandardRepository jobStandardRepository;
    private final TechStandardRepository techStandardRepository;

    public ProjectRegistrationViewDTO getView() {

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

        return new ProjectRegistrationViewDTO(
                jobs,
                techs,
                Arrays.asList(Project.ProjectType.values()),
                Arrays.asList(TechReqLevel.values())
        );
    }
}

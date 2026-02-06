package com.moirai.alloc.management.command.service;


import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.AssignmentShortageCalculator;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.service.GetAssignedStatus;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SelectAdditionalAssignmentCandidates {

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;
    private final GetAssignedStatus getAssignmentStatus;
    private final CandidateSelectionService candidateSelectionService;
    private final AssignmentShortageCalculator shortageCalculator;
    private final ApplicationEventPublisher eventPublisher;

    public void selectAdditionalCandidates(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Map<Long, Integer> shortageByJobId =
                shortageCalculator.calculate(project);

        if (shortageByJobId.isEmpty()) {
            return;
        }

        AssignCandidateDTO additionalCandidates =
                candidateSelectionService.select(project, shortageByJobId);
        List<JobAssignmentDTO> assignments =
                (additionalCandidates == null || additionalCandidates.getAssignments() == null)
                        ? Collections.emptyList()
                        : additionalCandidates.getAssignments();

        for (JobAssignmentDTO assignment : assignments) {
            if (assignment == null || assignment.getCandidates() == null) continue;

            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                if (candidate == null || candidate.getUserId() == null) continue;

                Long userId = candidate.getUserId();

                if (assignmentRepository.existsByProjectIdAndUserId(
                        projectId,
                        userId
                )) {
                    continue;
                }

                SquadAssignment saved = assignmentRepository.save(
                        SquadAssignment.propose(
                                projectId,
                                userId,
                                candidate.getFitnessScore()
                        )
                );

                eventPublisher.publishEvent(
                        new ProjectTempAssignmentEvent(
                                projectId,
                                project.getName(),
                                saved.getUserId()
                        )
                );
            }

        }
        log.info("shortageMap = {}", shortageByJobId);

    }
}
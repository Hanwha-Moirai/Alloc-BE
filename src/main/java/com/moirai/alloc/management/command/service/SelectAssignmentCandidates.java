package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.AssignmentShortageCalculator;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//        1) projectId로 프로젝트를 조회한다
//        2) policy 기반 후보 리스트를 조회하고 사용자가 선택한다
//        3) 선택 결과가 직군별 requiredCount를 충족하는지 검증한다
//        4) 검증된 선택 결과를 배정 후보로 저장한다

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class SelectAssignmentCandidates {

    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GetAssignmentCandidates getAssignmentCandidates;

    public void selectAssignmentCandidates(AssignCandidateDTO command) {

        if (command == null) {
            log.warn("selectAssignmentCandidates called with null command");
            return;
        }

        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        validateInitialSelection(project, command);

        for (JobAssignmentDTO assignment : command.getAssignments()) {

            if (assignment == null || assignment.getCandidates() == null) continue;

            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                if (candidate == null || candidate.getUserId() == null) continue;

                if (assignmentRepository.existsByProjectIdAndUserId(
                        project.getProjectId(),
                        candidate.getUserId()
                )) continue;

                SquadAssignment saved = assignmentRepository.save(
                        SquadAssignment.propose(
                                project.getProjectId(),
                                candidate.getUserId(),
                                candidate.getFitnessScore()
                        )
                );

                eventPublisher.publishEvent(
                        new ProjectTempAssignmentEvent(
                                project.getProjectId(),
                                project.getName(),
                                saved.getUserId()
                        )
                );
            }
        }
    }

    private void validateInitialSelection(Project project, AssignCandidateDTO command) {

        List<JobAssignmentDTO> commandAssignments =
                command.getAssignments() == null
                        ? Collections.emptyList()
                        : command.getAssignments();

        Map<Long, JobAssignmentDTO> selectionMap =
                commandAssignments.stream()
                        .collect(Collectors.toMap(
                                JobAssignmentDTO::getJobId,
                                a -> a
                        ));

        for (JobRequirement req : project.getJobRequirements()) {

            JobAssignmentDTO selection = selectionMap.get(req.getJobId());

            int actual = selection == null
                    ? 0
                    : selection.getCandidates().size();

            int expected = req.getRequiredCount();

            if (actual != expected) {
                throw new IllegalArgumentException(
                        "Must select exactly "
                                + expected
                                + " candidates for jobId="
                                + req.getJobId()
                );
            }
        }
    }

    public void selectByUserIds(Long projectId, List<Long> userIds) {

        if (userIds == null || userIds.isEmpty()) {
            log.warn("selectByUserIds called with empty userIds projectId={}", projectId);
            return;
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        AssignmentCandidatePageView page =
                getAssignmentCandidates.getAssignmentCandidates(projectId, null);

        if (page == null || page.getCandidates() == null) return;

        Map<Long, List<ScoredCandidateDTO>> groupedByJob =
                page.getCandidates().stream()
                        .filter(item -> userIds.contains(item.getUserId()))
                        .collect(Collectors.groupingBy(
                                AssignmentCandidateItemDTO::getJobId,
                                Collectors.mapping(
                                        item -> new ScoredCandidateDTO(
                                                item.getUserId(),
                                                item.getSkillScore()
                                                        + item.getExperienceScore()
                                                        + item.getAvailabilityScore()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        List<JobAssignmentDTO> assignments =
                groupedByJob.entrySet().stream()
                        .map(e -> new JobAssignmentDTO(e.getKey(), e.getValue()))
                        .toList();

        selectAssignmentCandidates(new AssignCandidateDTO(projectId, assignments));
    }
}

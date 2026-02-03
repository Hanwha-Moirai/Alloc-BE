package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.project.command.domain.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SelectAssignmentCandidates {
//        1) projectId로 프로젝트를 조회한다
//        2) policy 기반 후보 리스트를 조회하고 사용자가 선택한다
//        3) 선택 결과가 직군별 requiredCount를 충족하는지 검증한다
//        4) 검증된 선택 결과를 배정 후보로 저장한다


    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SelectAssignmentCandidates(
            SquadAssignmentRepository assignmentRepository,
            ProjectRepository projectRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.eventPublisher = eventPublisher;
    }

    public void selectAssignmentCandidates(AssignCandidateDTO command) {

        // 1) 프로젝트 조회
        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2) 직군별 선택 인원 검증 (정확히 requiredCount만큼 선택했는지)
        validateSelectedCounts(project, command);

        // 3) 신규 후보 생성
        for (JobAssignmentDTO assignment : command.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {
                Long userId = candidate.getUserId();

                if (assignmentRepository.existsByProjectIdAndUserId(
                        project.getProjectId(), userId)) {
                    continue;
                }

                SquadAssignment saved = assignmentRepository.save(
                        SquadAssignment.propose(
                                project.getProjectId(),
                                userId,
                                candidate.getFitnessScore()
                        )
                );
                ProjectTempAssignmentEvent event = new ProjectTempAssignmentEvent(
                        project.getProjectId(),
                        project.getName(),
                        saved.getUserId()
                );
                eventPublisher.publishEvent(event);
                log.info("Published ProjectTempAssignmentEvent projectId={} userId={} projectName={}",
                        event.projectId(), event.userId(), event.projectName());
            }
        }
    }

    //직군별로 requiredCount를 정확히 충족했는지 검증
    private void validateSelectedCounts(
            Project project,
            AssignCandidateDTO command
    ) {
        Map<Long, JobAssignmentDTO> selectionMap =
                command.getAssignments().stream()
                        .collect(Collectors.toMap(
                                JobAssignmentDTO::getJobId,
                                Function.identity()
                        ));

        for (JobRequirement requirement : project.getJobRequirements()) {

            JobAssignmentDTO selection =
                    selectionMap.get(requirement.getJobId());

            if (selection == null) {
                throw new IllegalArgumentException(
                        "No candidates selected for jobId=" + requirement.getJobId()
                );
            }

            if (selection.getCandidates().size()
                    != requirement.getRequiredCount()) {
                throw new IllegalArgumentException(
                        "Must select exactly "
                                + requirement.getRequiredCount()
                                + " candidates for jobId="
                                + requirement.getJobId()
                );
            }
        }
    }
}

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

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SelectAdditionalAssignmentCandidates {
// 1) 프로젝트 조회
// 2) AssignmentShortageCalculator(policy)로 부족 인원 계산
// 3) 부족 인원 기준으로 CandidateSelectionService(policy)에 후보 추천 위임
// 4) 중복을 제외하고 신규 후보를 SquadAssignment로 저장

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;
    private final GetAssignedStatus getAssignmentStatus;
    private final CandidateSelectionService candidateSelectionService;
    private final AssignmentShortageCalculator shortageCalculator;
    private final ApplicationEventPublisher eventPublisher;

    public void selectAdditionalCandidates(Long projectId) {

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 부족 인원 계산 - policy
        Map<Long, Integer> shortageByJobId =
                shortageCalculator.calculate(project);

        // 부족 인원 없으면 종료
        if (shortageByJobId.isEmpty()) {
            return;
        }
        //부족한 인원 기준 후보 추천
        AssignCandidateDTO additionalCandidates =
                candidateSelectionService.select(project, shortageByJobId);

        // 신규 후보 저장
        for (JobAssignmentDTO assignment : additionalCandidates.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                Long userId = candidate.getUserId();

                if (assignmentRepository.existsByProjectIdAndUserId(
                        projectId, userId)) {
                    continue;
                }

                SquadAssignment saved = assignmentRepository.save(
                        SquadAssignment.propose(
                                projectId,
                                userId,
                                candidate.getFitnessScore()
                        )
                );
                ProjectTempAssignmentEvent event = new ProjectTempAssignmentEvent(
                        projectId,
                        project.getName(),
                        saved.getUserId()
                );
                eventPublisher.publishEvent(event);
                log.info("Published ProjectTempAssignmentEvent projectId={} userId={} projectName={}",
                        event.projectId(), event.userId(), event.projectName());
            }
        }
    }
}

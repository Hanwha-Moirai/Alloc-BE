package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.service.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.management.query.service.GetAssignmentStatus;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class SelectAdditionalAssignmentCandidates {
//      1) 프로젝트 조회
//      2) GetAssignmentStatus 로 부족 인원 조회
//      3) 부족한 직군만 Map<Long, Integer>로 추출
//      4) CandidateSelectionService(policy)에 위임
//      5) 추가 후보를 SquadAssignment 리포지토리 로 저장

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;
    private final GetAssignmentStatus getAssignmentStatus;
    private final CandidateSelectionService candidateSelectionService;

    public void selectAdditionalCandidates(Long projectId) {

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 현재 상태 조회
        AssignmentStatusDTO status =
                getAssignmentStatus.getStatus(projectId);

        // 부족 인원 없으면 종료
        if (!status.hasShortage()) {
            return;
        }

        Map<Long, Integer> shortageByJobId =
                status.getShortageByJobId();

        AssignCandidateDTO additionalCandidates =
                candidateSelectionService.select(project, shortageByJobId);

        for (JobAssignmentDTO assignment : additionalCandidates.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                Long userId = candidate.getUserId();

                if (assignmentRepository.existsByProjectIdAndUserId(
                        projectId, userId)) {
                    continue;
                }

                assignmentRepository.save(
                        SquadAssignment.propose(
                                projectId,
                                userId,
                                candidate.getFitnessScore()
                        )
                );
            }
        }
    }
}
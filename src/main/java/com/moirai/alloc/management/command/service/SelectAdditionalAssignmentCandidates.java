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

@Service
@Transactional
@RequiredArgsConstructor
public class SelectAdditionalAssignmentCandidates {
//        1. 프로젝트 조회
//        2. Query Service에게 현재 상태를 물어봄
//        3. 부족 인원 수를 결과로 받음
//        4. policy로 “추가 후보 userId 목록” 계산
//        5. SquadAssignment.propose()로 추가 생성
//        6.저장
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

        // policy로 추가 후보 계산
        AssignCandidateDTO additionalCandidates =
                candidateSelectionService.select(project, status);

        // 후보 생성 (candidates 기준)
        for (JobAssignmentDTO assignment : additionalCandidates.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                Long userId = candidate.getUserId();
                int fitnessScore = candidate.getFitnessScore();

                // 중복 방지
                if (assignmentRepository.existsByProjectIdAndUserId(projectId, userId)) {
                    continue;
                }

                assignmentRepository.save(
                        SquadAssignment.propose(projectId, userId, fitnessScore)
                );
            }
        }
    }
}

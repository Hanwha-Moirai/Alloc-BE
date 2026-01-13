package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SelectAssignmentCandidates {
//        1) projectId로 프로젝트를 조회한다
//        2) policy 기반 후보 리스트를 조회하고 사용자가 선택한다
//        3) 선택 결과가 직군별 requiredCount를 충족하는지 검증한다
//        4) 검증된 선택 결과를 배정 후보로 저장한다


    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;

    public SelectAssignmentCandidates(
            SquadAssignmentRepository assignmentRepository,
            ProjectRepository projectRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
    }

    public void selectAssignmentCandidates(AssignCandidateDTO command) {

        // 1) 프로젝트 조회
        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2) 직군별 선택 인원 검증 (정확히 requiredCount만큼 선택했는지)
        validateSelectedCounts(project, command);

        // 3) 배정 후보 저장
        for (JobAssignmentDTO assignment : command.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                Long userId = candidate.getUserId();
                int fitnessScore = candidate.getFitnessScore();

                // 이미 후보로 존재하면 스킵 (멱등성 보장)
                if (assignmentRepository.existsByProjectIdAndUserId(
                        project.getProjectId(), userId)) {
                    continue;
                }

                assignmentRepository.save(
                        SquadAssignment.propose(
                                project.getProjectId(),
                                userId,
                                fitnessScore
                        )
                );
            }
        }

    }

    //직군별로 requiredCount를 정확히 충족했는지 검증
    private void validateSelectedCounts(Project project, AssignCandidateDTO command) {

        for (JobRequirement requirement : project.getJobRequirements()) {

            JobAssignmentDTO selection =
                    findAssignment(command, requirement.getJobId());

            if (selection == null) {
                throw new IllegalArgumentException(
                        "No candidates selected for required jobId=" + requirement.getJobId()
                );
            }

            if (selection.getCandidates().size() != requirement.getRequiredCount()) {
                throw new IllegalArgumentException(
                        "Must select exactly " + requirement.getRequiredCount()
                                + " candidates for jobId=" + requirement.getJobId()
                );
            }
        }
    }

    //특정 직군(jobId)에 대한 사용자 선택 결과 조회
    private JobAssignmentDTO findAssignment(
            AssignCandidateDTO command, Long jobId) {

        for (JobAssignmentDTO assignment : command.getAssignments()) {
            if (assignment.getJobId().equals(jobId)) {
                return assignment;
            }
        }
        return null;
    }
}

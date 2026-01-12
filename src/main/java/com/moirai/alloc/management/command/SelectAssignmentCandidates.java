package com.moirai.alloc.management.command;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.CandidateSelectionPolicy;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class SelectAssignmentCandidates {
    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;

    public SelectAssignmentCandidates(SquadAssignmentRepository assignmentRepository, ProjectRepository projectRepository, CandidateSelectionPolicy candidateSelectionPolicy) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
    }
    public void selectAssignmentCandidates(AssignCandidateDTO command) {
//        1) projectId로 프로젝트를 조회한다.
//        2) policy에 따라 후보 인력 계산한다
//        3) 계산된 후보인력을 '배정후보'로 생성한다
//        4) 배정 후보를 저장한다
        // 1) 프로젝트 조회
        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2) 직군별 필요 인원 검증
        for (JobRequirement requirement : project.getJobRequirements()) {

            JobAssignmentDTO assignment =
                    findAssignment(command, requirement.getJobId());

            if (assignment == null ||
                    assignment.getUserIds().size() != requirement.getRequiredCount()) {

                throw new IllegalArgumentException(
                        "Required count not met for jobId=" + requirement.getJobId()
                );
            }
        }

        // 3) 배정 후보 생성 및 저장
        for (JobAssignmentDTO assignment : command.getAssignments()) {
            for (Long userId : assignment.getUserIds()) {

                if (assignmentRepository.existsByProjectIdAndUserId(
                        project.getProjectId(), userId)) {
                    throw new IllegalStateException(
                            "User already assigned: userId=" + userId
                    );
                }

                assignmentRepository.save(
                        SquadAssignment.propose(project.getProjectId(), userId)
                );
            }
        }
    }

    // 직군별 선택 결과 찾기 (헬퍼 메서드)
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

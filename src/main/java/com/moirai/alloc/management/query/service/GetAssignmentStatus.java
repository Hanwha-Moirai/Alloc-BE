package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.management.query.dto.select_list.AssignmentSummaryCardDTO;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//        1) assignmentId로 배정을 식별한다.
//        2) 해당 배정의 최종 상태 정보 목록을 조회한다.
//        4) 최종 상태 정보 목록을 조회용 형태로 반환한다.

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAssignmentStatus {

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;

    //Command 판단용 (부족 인원 계산; assigned 기준으로)

    public AssignmentStatusDTO getStatus(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Map<Long, Integer> shortageByJobId = new HashMap<>();

        for (JobRequirement req : project.getJobRequirements()) {

            long assignedCount =
                    assignmentRepository.countAssignedByProjectAndJob(
                            projectId,
                            req.getJobId()
                    );

            int shortage =
                    req.getRequiredCount() - (int) assignedCount;

            shortageByJobId.put(
                    req.getJobId(),
                    Math.max(shortage, 0)
            );
        }

        return new AssignmentStatusDTO(shortageByJobId);
    }

    //요약카드; 직원 응답 상태 기준
    public AssignmentSummaryCardDTO getSummary(Long projectId) {

        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        int total = assignments.size();

        int requested = (int) assignments.stream()
                .filter(a -> a.getAssignmentStatus() == AssignmentStatus.REQUESTED)
                .count();

        int accepted = (int) assignments.stream()
                .filter(a -> a.getAssignmentStatus() == AssignmentStatus.ACCEPTED)
                .count();

        int interview = (int) assignments.stream()
                .filter(a -> a.getAssignmentStatus() == AssignmentStatus.INTERVIEW_REQUESTED)
                .count();

        return new AssignmentSummaryCardDTO(
                total,
                requested,
                accepted,
                interview
        );
    }
}

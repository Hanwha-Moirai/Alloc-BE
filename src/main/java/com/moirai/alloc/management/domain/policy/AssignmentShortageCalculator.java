package com.moirai.alloc.management.domain.policy;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AssignmentShortageCalculator {
    //현재 프로젝트 요구사항 대비 최종 배정된 인원 기준 부족 인원 계산

    private final SquadAssignmentRepository assignmentRepository;

    public Map<Long, Integer> calculate(Project project) {

        Map<Long, Integer> selectCountByJobId = new HashMap<>();

        Long projectId = project.getProjectId();

        for (JobRequirement req : project.getJobRequirements()) {

            Long jobId = req.getJobId();

            int required = req.getRequiredCount();

            long assigned =
                    assignmentRepository.countAssignedByProjectAndJob(
                            projectId,
                            jobId
                    );

            long pending =
                    assignmentRepository.countPendingByProjectAndJob(
                            projectId,
                            jobId
                    );

            long excluded =
                    assignmentRepository.countExcludedByProjectAndJob(
                            projectId,
                            jobId
                    );

            int capacityShortage =
                    required - (int)(assigned + pending);



            if (capacityShortage < 0) capacityShortage = 0;

            int finalSelectCount = capacityShortage + (int) excluded;

            if (finalSelectCount > 0) {
                selectCountByJobId.put(jobId, finalSelectCount);
            }
        }

        return selectCountByJobId;
    }

    public boolean hasShortage(Project project) {
        return !calculate(project).isEmpty();
    }
}

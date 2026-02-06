package com.moirai.alloc.management.domain.policy;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScore;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScoringService;
import com.moirai.alloc.management.domain.policy.scoring.WeightPolicy;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CandidateSelectionService {
    // 직군별 직원 조회, 점수 계산 + 가중치 + 정렬 + 3배수 컷 + 결과 dto 생성

    private final EmployeeRepository employeeRepository;
    private final CandidateScoringService candidateScoringService;
    private final WeightPolicy weightPolicy;
    private final SquadAssignmentRepository assignmentRepository;

    public AssignCandidateDTO select(
            Project project,
            Map<Long, Integer> requiredCountByJobId
    ) {
        List<JobAssignmentDTO> assignments = new ArrayList<>();
        log.info("===== Candidate Selection START projectId={} =====", project.getProjectId());

        for (JobRequirement jobReq : project.getJobRequirements()) {

            Long jobId = jobReq.getJobId();
            int requiredCount =
                    requiredCountByJobId.getOrDefault(jobId, 0);

            log.info("jobId={} requiredCount={}", jobId, requiredCount);

            if (requiredCount <= 0) {
                log.info("jobId={} SKIP (requiredCount <= 0)", jobId);
                continue;
            }
            // 3배수 컷
            int limit = requiredCount * 3;

            // 직군별 직원 조회
            List<Employee> employees =
                    employeeRepository.findByJobId(jobId);

            log.info("jobId={} employees.size={}", jobId, employees.size());

            Set<Long> blockedUserIds =
                    new HashSet<>(
                            assignmentRepository.findUserIdsInProjectByDecision(
                                    project.getProjectId(),
                                    FinalDecision.ASSIGNED,
                                    employees.stream().map(Employee::getUserId).toList()
                            )
                    );
            log.info("jobId={} blocked(ASSIGNED).size={}", jobId, blockedUserIds.size());

            List<ScoredCandidateDTO> candidates =
                    employees.stream()
                            .filter(emp -> !blockedUserIds.contains(emp.getUserId()))
                            .map(emp -> {
                                // 점수 계산
                                CandidateScore raw =
                                        candidateScoringService.score(project, emp);
                                //가중치 적용
                                int weightedScore =
                                        weightPolicy.apply(project, raw);
                                return new ScoredCandidateDTO(
                                        emp.getUserId(),
                                        weightedScore
                                );
                            })
                            // 정렬(내림차순)
                            .sorted(Comparator
                                    .comparingInt(ScoredCandidateDTO::getFitnessScore)
                                    .reversed())
                            .limit(limit)
                            .toList();
            log.info("jobId={} candidatesAfterFilter={}", jobId, candidates.size());

            // 결과 dto 생성
            assignments.add(
                    new JobAssignmentDTO(jobId, candidates)
            );
        }
        log.info("===== Candidate Selection END =====");
        return new AssignCandidateDTO(
                project.getProjectId(),
                assignments
        );

    }
}

package com.moirai.alloc.management.domain.policy.service;

import com.moirai.alloc.management.EmployeeRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScore;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScoringService;
import com.moirai.alloc.management.domain.policy.scoring.WeightPolicy;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.select_list.AssignmentStatusDTO;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CandidateSelectionService {

    private final EmployeeRepository employeeRepository;
    private final CandidateScoringService candidateScoringService;
    private final WeightPolicy weightPolicy;


    public AssignCandidateDTO select(
            Project project,
            Map<Long, Integer> requiredCountByJobId
    ) {
        List<JobAssignmentDTO> assignments = new ArrayList<>();

        for (JobRequirement jobReq : project.getJobRequirements()) {

            Long jobId = jobReq.getJobId();

            int requiredCount =
                    requiredCountByJobId.getOrDefault(jobId, 0);

            if (requiredCount <= 0) {
                continue;
            }
            // 3배수 컷
            int limit = requiredCount * 3;
            //1) 직군별 직원 조회
            List<Employee> employees =
                    employeeRepository.findByJobId(jobId);

            List<ScoredCandidateDTO> candidates =
                    employees.stream()
                            .map(emp -> {
                                // 점수 계산
                                CandidateScore raw =
                                        candidateScoringService.score(project, emp);
                                //가중치 적용
                                int score =
                                        weightPolicy.apply(project, raw);
                                return new ScoredCandidateDTO(
                                        emp.getUserId(),
                                        score
                                );
                            })
                            // 정렬(내림차순)
                            .sorted(Comparator
                                    .comparingInt(ScoredCandidateDTO::getFitnessScore)
                                    .reversed())
                            .limit(limit)
                            .toList();
            // 결과 dto 생성
            assignments.add(
                    new JobAssignmentDTO(jobId, candidates)
            );
        }

        return new AssignCandidateDTO(
                project.getProjectId(),
                assignments
        );

    }
}


// 직군별 직원 조회, 점수 계산 + 가중치 + 정렬 + 3배수 컷 + 결과 dto 생성

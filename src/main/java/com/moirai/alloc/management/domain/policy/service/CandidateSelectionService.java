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
@Component
@RequiredArgsConstructor
public class CandidateSelectionService {

    private final EmployeeRepository employeeRepository;
    private final CandidateScoringService candidateScoringService;
    private final WeightPolicy weightPolicy;

    /**
     * 프로젝트 기준 후보 계산
     * - 최초 선발: status == null
     * - 추가 선발: status != null (부족 인원 기준)
     */
    public AssignCandidateDTO select(
            Project project,
            AssignmentStatusDTO status
    ) {
        List<JobAssignmentDTO> assignments = new ArrayList<>();

        for (JobRequirement jobReq : project.getJobRequirements()) {

            Long jobId = jobReq.getJobId();

            // ⭐ 핵심: 몇 명 필요한지는 policy가 결정
            int requiredCount =
                    (status == null)
                            ? jobReq.getRequiredCount()
                            : status.getShortage(jobId);

            if (requiredCount <= 0) {
                continue;
            }

            int limit = requiredCount * 3;

            // 직군별 직원 조회
            List<Employee> employees =
                    employeeRepository.findByJobId(jobId);

            // 점수 계산 + 가중치 + 정렬
            List<ScoredCandidateDTO> candidates = employees.stream()
                    .map(emp -> {
                        CandidateScore raw =
                                candidateScoringService.score(project, emp);

                        int weighted =
                                weightPolicy.apply(project, raw);

                        return new ScoredCandidateDTO(
                                emp.getUserId(),
                                weighted
                        );
                    })
                    .sorted(Comparator.comparingInt(ScoredCandidateDTO::getFitnessScore).reversed())
                    .limit(limit)
                    .toList();

            assignments.add(new JobAssignmentDTO(jobId, candidates));

        }

        return new AssignCandidateDTO(project.getProjectId(), assignments);
    }

    private record ScoredUser(Long userId, int fitnessScore) {}
}


// 직군별 직원 조회, 점수 계산 + 가중치 + 정렬 + 3배수 컷 + 결과 dto 생성
// 최초 선발, 추가 선발까지 담당.
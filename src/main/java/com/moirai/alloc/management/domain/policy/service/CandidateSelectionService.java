package com.moirai.alloc.management.domain.policy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CandidateSelectionService {

    /*private final EmployeeRepository employeeRepository;
    private final CandidateScoringService candidateScoringService;
    private final WeightPolicy weightPolicy;

    public AssignCandidateDTO select(Project project) {

        List<JobAssignmentDTO> assignments = new ArrayList<>();

        for (JobRequirement jobReq : project.getJobRequirements()) {

            Long jobId = jobReq.getJobId();
            int limit = jobReq.getRequiredCount() * 3;

            // 1) 해당 직군 직원 후보 조회
            List<Employee> employees = employeeRepository.findByJobId(jobId);

            // 2) 점수 계산 + 가중치 적용 + 정렬 + 상위 N명 추출
            List<Long> userIds = employees.stream()
                    .map(emp -> {
                        CandidateScore raw = candidateScoringService.score(project, emp);
                        int weighted = weightPolicy.apply(project, raw);
                        return new ScoredUser(emp.getUserId(), weighted);
                    })
                    .sorted(Comparator.comparingInt(ScoredUser::score).reversed())
                    .limit(limit)
                    .map(ScoredUser::userId)
                    .toList();

            assignments.add(new JobAssignmentDTO(jobId, userIds));
        }

        return new AssignCandidateDTO(project.getProjectId(), assignments);
    }

    // 내부 정렬용 (후보 정렬용)
    private record ScoredUser(Long userId, int score) {}*/
}

// 직군별 직원 조회, 점수 계산 + 가중치 + 정렬 + 3배수 컷 + 결과 dto 생성
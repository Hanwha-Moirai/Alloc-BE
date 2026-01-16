package com.moirai.alloc.management.query.service;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidate_list.JobAssignmentSummaryDTO;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidatesView;
import com.moirai.alloc.management.query.dto.controller_dto.AssignmentCandidatePageView;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAssignmentCandidates {
//    1) 프로젝트 및 직군 요구 사항 조회
//    2) policy 기반 후보 계산 조회 (읽기 전용)
//    3) 현재 선택된 인원 조회
//    4) 선택된 인원 + 추천 후보를 하나의 리스트로 병합
//    5) 각 인원에 대해 selected / workStatus 상태 계산

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JobStandardRepository jobStandardRepository;
    private final CandidateSelectionService candidateSelectionService;

    public AssignmentCandidatePageView getAssignmentCandidates(Long projectId) {

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // policy 기반 추천 후보 계산 조회 - 추천 정책 계산에 필요한 입력 값
        Map<Long, Integer> requiredCountByJobId =
                project.getJobRequirements().stream()
                        .collect(Collectors.toMap(
                                JobRequirement::getJobId,
                                JobRequirement::getRequiredCount
                        ));

        //Policy 기반 추천 후보 계산 - 아직 선택되지 않은 잠재 후보
        AssignCandidateDTO recommended =
                candidateSelectionService.select(project, requiredCountByJobId);


        // 현재까지 선발된(선택) 후보 조회; summary 용
        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        Set<Long> selectedUserIds =
                assignments.stream()
                        .map(SquadAssignment::getUserId)
                        .collect(Collectors.toSet());

        // employee 조회 범위 결정; 선발된 인원 + 추천 후보
        Set<Long> recommendedUserIds =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .map(ScoredCandidateDTO::getUserId)
                        .collect(Collectors.toSet());

        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(selectedUserIds);
        allUserIds.addAll(recommendedUserIds);

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllById(allUserIds).stream()
                        .collect(Collectors.toMap(
                                Employee::getUserId,
                                Function.identity()
                        ));


        //Job 이름 맵 (jobId → jobName)
        Map<Long, String> jobNameMap =
                jobStandardRepository.findAllById(
                                project.getJobRequirements().stream()
                                        .map(JobRequirement::getJobId)
                                        .toList()
                        )
                        .stream()
                        .collect(Collectors.toMap(
                                JobStandard::getJobId,
                                JobStandard::getJobName
                        ));

        //Job 요약 생성
        List<JobAssignmentSummaryDTO> jobSummaries =
                project.getJobRequirements().stream()
                        .map(req -> createJobSummary(
                                req,
                                assignments,
                                employeeMap,
                                jobNameMap.get(req.getJobId())
                        ))
                        .toList();

        // 현재 근무 중인 인원 Set (N+1 제거) - 근무 상태(workStatus) 계산에 사용
        Set<Long> workingUserIds =
                assignmentRepository.findUserIdsByFinalDecision(FinalDecision.ASSIGNED);

       //선택된 인원 DTO (selected = true)
        List<AssignmentCandidateItemDTO> selectedCandidates =
                assignments.stream()
                        .map(a -> {
                            Employee e = employeeMap.get(a.getUserId());
                            if (e == null) return null;

                            User u = e.getUser();

                            AssignmentCandidateItemDTO.WorkStatus workStatus =
                                    workingUserIds.contains(u.getUserId())
                                            ? AssignmentCandidateItemDTO.WorkStatus.ASSIGNED
                                            : AssignmentCandidateItemDTO.WorkStatus.AVAILABLE;

                            return new AssignmentCandidateItemDTO(
                                    u.getUserId(),
                                    u.getUserName(),
                                    e.getJob().getJobName(),
                                    resolveMainSkill(e),
                                    e.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    a.getFitnessScore(),
                                    true //  선택됨
                            );
                        })
                        .filter(Objects::nonNull)
                        .toList();


        // 추천 후보 DTO 생성 (selected = false)  - 이미 선택된 인원은 중복 제거
        List<AssignmentCandidateItemDTO> recommendedCandidates =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .filter(c -> !selectedUserIds.contains(c.getUserId())) // 중복 제거
                        .map(c -> {
                            Employee e = employeeMap.get(c.getUserId());
                            if (e == null) return null;

                            User u = e.getUser();

                            AssignmentCandidateItemDTO.WorkStatus workStatus =
                                    workingUserIds.contains(u.getUserId())
                                            ? AssignmentCandidateItemDTO.WorkStatus.ASSIGNED
                                            : AssignmentCandidateItemDTO.WorkStatus.AVAILABLE;

                            return new AssignmentCandidateItemDTO(
                                    u.getUserId(),
                                    u.getUserName(),
                                    e.getJob().getJobName(),
                                    resolveMainSkill(e),
                                    e.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    c.getFitnessScore(),
                                    false // 아직 미선택
                            );
                        })
                        .filter(Objects::nonNull)
                        .toList();

        //최종 후보 리스트 병합
        List<AssignmentCandidateItemDTO> candidates =
                Stream.concat(
                        selectedCandidates.stream(),
                        recommendedCandidates.stream()
                ).toList();

        return new AssignmentCandidatePageView(jobSummaries, candidates);
    }

    // 헬퍼 메서드
    private JobAssignmentSummaryDTO createJobSummary(
            JobRequirement req,
            List<SquadAssignment> assignments,
            Map<Long, Employee> employeeMap,
            String jobName
    ) {
        long selectedCount =
                assignments.stream()
                        .filter(SquadAssignment::isPending)
                        .filter(a -> {
                            Employee e = employeeMap.get(a.getUserId());
                            return e != null
                                    && e.getJob().getJobId().equals(req.getJobId());
                        })
                        .count();

        JobAssignmentSummaryDTO.Status status =
                selectedCount == 0
                        ? JobAssignmentSummaryDTO.Status.NONE
                        : selectedCount < req.getRequiredCount()
                        ? JobAssignmentSummaryDTO.Status.INCOMPLETE
                        : JobAssignmentSummaryDTO.Status.COMPLETE;

        return new JobAssignmentSummaryDTO(
                req.getJobId(),
                jobName,
                (int) selectedCount,
                req.getRequiredCount(),
                status
        );
    }

    private String resolveMainSkill(Employee employee) {
        return employee.getSkills().stream()
                .max(Comparator.comparingInt(
                        skill -> skill.getProficiency().ordinal()
                ))
                .map(skill -> skill.getTech().getTechName())
                .orElse(null);
    }
}
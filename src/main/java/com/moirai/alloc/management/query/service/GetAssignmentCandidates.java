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
//    1) 프로젝트 조회
//    2) policy 기반 후보 계산 조회 (읽기 전용)
//    3) 선발 중인 현황 요약
//    4) policy 기반 추천 후보 DTO 반환 (아직 미선발)

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JobStandardRepository jobStandardRepository;
    private final CandidateSelectionService candidateSelectionService;

    public AssignmentCandidatesView getAssignmentCandidates(Long projectId) {

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // policy 기반 추천 후보 계산 조회
        Map<Long, Integer> requiredCountByJobId =
                project.getJobRequirements().stream()
                        .collect(Collectors.toMap(
                                JobRequirement::getJobId,
                                JobRequirement::getRequiredCount
                        ));

        AssignCandidateDTO recommended =
                candidateSelectionService.select(project, requiredCountByJobId);


        // 현재까지 선발된(선택) 후보 조회; summary 용
        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        // employee 조회 범위 결정; 선발된 인원 + 추천 후보
        //선발된 인원
        List<Long> assignedUserIds =
                assignments.stream()
                        .map(SquadAssignment::getUserId)
                        .distinct().toList();
        //추천된(후보)인원
        List<Long> recommendedUserIds =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .map(ScoredCandidateDTO::getUserId)
                        .distinct()
                        .toList();
        //선발된 인원 + 추천된(후보)인원의 합집합
        List<Long> allUserIds =
                Stream.concat(
                        assignedUserIds.stream(),
                        recommendedUserIds.stream()
                ).distinct().toList();

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

        // 현재 근무 중인 인원 Set (N+1 제거)
        Set<Long> workingUserIds =
                assignmentRepository.findUserIdsByFinalDecision(FinalDecision.ASSIGNED);

        // 후보 리스트 DTO 생성; policy 결과 기반 (선택 안 된 추천 후보)
        List<AssignmentCandidateItemDTO> candidates =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .map(c -> createCandidateItem(
                                c,
                                employeeMap.get(c.getUserId()),
                                workingUserIds
                        ))
                        .filter(Objects::nonNull)
                        .toList();


        return new AssignmentCandidatesView(jobSummaries, candidates);
    }

    // Query 전용 View DTO 조립 헬퍼
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

    private AssignmentCandidateItemDTO createCandidateItem(
            ScoredCandidateDTO c,
            Employee employee,
            Set<Long> workingUserIds
    ) {
        if (employee == null) {
            return null;
        }

        User user = employee.getUser();

        AssignmentCandidateItemDTO.WorkStatus workStatus =
                workingUserIds.contains(employee.getUserId())
                        ? AssignmentCandidateItemDTO.WorkStatus.ASSIGNED
                        : AssignmentCandidateItemDTO.WorkStatus.AVAILABLE;

        return new AssignmentCandidateItemDTO(
                user.getUserId(),
                user.getUserName(),
                employee.getJob().getJobName(),
                resolveMainSkill(employee),
                employee.getTitleStandard().getMonthlyCost(),
                workStatus,
                c.getFitnessScore(),
                false
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


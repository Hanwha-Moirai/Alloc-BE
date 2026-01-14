package com.moirai.alloc.management.query.service;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.management.EmployeeRepository;
import com.moirai.alloc.management.JobStandardRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.service.CandidateSelectionService;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidate_list.JobAssignmentSummaryDTO;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidatesView;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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


    private String resolveMainSkill(Employee employee) {
        return employee.getSkills().stream()
                .max(Comparator.comparingInt(
                        skill -> skill.getProficiency().ordinal()
                ))
                .map(skill -> skill.getTech().getTechName())
                .orElse(null);
    }

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

        // 사용자 ID
        List<Long> userIds =
                assignments.stream()
                        .map(SquadAssignment::getUserId)
                        .distinct().toList();

        // User / Employee 일괄 조회
        Map<Long, User> userMap =
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(
                                User::getUserId,
                                Function.identity()
                        ));

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllById(userIds).stream()
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

        //Job 요약 DTO 생성
        List<JobAssignmentSummaryDTO> jobSummaries =
                project.getJobRequirements().stream()
                        .map(req -> {
                            // 최종 결정되지 않은(선발 중인) 인원 기준
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
                                    jobNameMap.get(req.getJobId()),
                                    (int) selectedCount,
                                    req.getRequiredCount(),
                                    status
                            );
                        })
                        .toList();
        /* =========================
           6. 현재 근무 중인 인원 Set (N+1 제거)
           ========================= */
        Set<Long> workingUserIds =
                assignmentRepository.findUserIdsByFinalDecision(FinalDecision.ASSIGNED);


        // 후보 리스트 DTO 생성; policy 결과 기반
        // 후보 리스트 DTO 생성; policy 결과 기반 (선택 안 된 추천 후보)
        List<AssignmentCandidateItemDTO> candidates =
                recommended.getAssignments().stream()
                        .flatMap(job -> job.getCandidates().stream())
                        .map(c -> {

                            Employee employee = employeeMap.get(c.getUserId());
                            if (employee == null) {
                                return null; // 데이터 불일치 방어
                            }
                            User user = employee.getUser();

                            AssignmentCandidateItemDTO.WorkStatus workStatus =
                                    workingUserIds.contains(employee.getUserId())
                                            ? AssignmentCandidateItemDTO.WorkStatus.ASSIGNED
                                            : AssignmentCandidateItemDTO.WorkStatus.AVAILABLE;

                            return new AssignmentCandidateItemDTO(
                                    user.getUserId(),
                                    user.getUserName(),
                                    jobNameMap.get(employee.getJob().getJobId()),
                                    resolveMainSkill(employee),
                                    employee.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    c.getFitnessScore(),
                                    false            //미결정상태 조회
                            );
                        })
                        .filter(Objects::nonNull)
                        .toList();


        return new AssignmentCandidatesView(jobSummaries, candidates);
    }
}


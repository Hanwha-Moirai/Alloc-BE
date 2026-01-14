package com.moirai.alloc.management.query.service;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.management.EmployeeRepository;
import com.moirai.alloc.management.JobStandardRepository;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidate_list.JobAssignmentSummaryDTO;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidatesView;
import com.moirai.alloc.management.query.dto.candidate_list.JobAssignmentStatus;
import com.moirai.alloc.management.query.dto.candidate_list.WorkStatus;
import com.moirai.alloc.profile.common.domain.Employee;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//        2) projectId로 프로젝트를 식별한다
//        3) 해당 프로젝트에 대해 생성된 배정 후보 목록을 조회한다.
//        4) 후보 목록을 조회용 형태로 반환한다.

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAssignmentCandidates {

    private final ProjectRepository projectRepository;
    private final SquadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JobStandardRepository jobStandardRepository;

    private String resolveMainSkill(Employee employee) {
        return employee.getSkills().stream()
                .max(Comparator.comparingInt(
                        skill -> skill.getProficiency().ordinal()
                ))
                .map(skill -> skill.getTech().getTechName())
                .orElse(null);
    }

    public AssignmentCandidatesView getAssignmentCandidates(
            Long projectId
    ) {

        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 해당 프로젝트의 후보 목록 조회
        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        // 사용자 ID
        List<Long> userIds =
                assignments.stream()
                        .map(SquadAssignment::getUserId)
                        .toList();

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

                            long selectedCount =
                                    assignments.stream()
                                            .filter(SquadAssignment::isPending)
                                            .filter(a -> {
                                                Employee e = employeeMap.get(a.getUserId());
                                                return e.getJob().getJobId().equals(req.getJobId());
                                            })
                                            .count();


                            JobAssignmentStatus status;
                            if (selectedCount == 0) {
                                status = JobAssignmentStatus.NONE;
                            } else if (selectedCount < req.getRequiredCount()) {
                                status = JobAssignmentStatus.INCOMPLETE;
                            } else {
                                status = JobAssignmentStatus.COMPLETE;
                            }

                            return new JobAssignmentSummaryDTO(
                                    req.getJobId(),
                                    jobNameMap.get(req.getJobId()),
                                    (int) selectedCount,
                                    req.getRequiredCount(),
                                    status
                            );
                        })
                        .toList();



        // 후보 리스트 DTO 생성
        List<AssignmentCandidateItemDTO> candidates =
                assignments.stream()
                        .map(a -> {

                            User user = userMap.get(a.getUserId());
                            Employee employee = employeeMap.get(a.getUserId());

                            String jobName =
                                    jobNameMap.get(employee.getJob().getJobId());

                            String mainSkill = resolveMainSkill(employee);

                            WorkStatus workStatus =
                                    a.isFinallyAssigned()
                                            ? WorkStatus.ASSIGNED
                                            : WorkStatus.AVAILABLE;

                            return new AssignmentCandidateItemDTO(
                                    user.getUserId(),
                                    user.getUserName(),
                                    jobName,
                                    mainSkill,
                                    employee.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    a.getFitnessScore(),
                                    a.getFinalDecision() == FinalDecision.ASSIGNED
                            );
                        })
                        .toList();

        return new AssignmentCandidatesView(jobSummaries, candidates);
    }
}


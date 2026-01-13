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
import com.moirai.alloc.management.query.view.AssignmentCandidatesView;
import com.moirai.alloc.management.query.view.JobAssignmentStatus;
import com.moirai.alloc.management.query.view.WorkStatus;
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
import java.util.stream.Collectors;

//        2) projectId로 프로젝트를 식별한다
//        3) 해당 프로젝트에 대해 생성된 배정 후보 목록을 조회한다.
//        4) 후보 목록을 조회용 형태로 반환한다.
/**
 * 배정 후보 조회 (Query)
 * - 이미 생성된 배정 후보를 화면에 표시하기 위한 조회 전용 서비스
 * - 권한 검증은 상위 계층(Controller / Security)에서 수행
 */
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
                                            .filter(a ->
                                                    a.getFinalDecision() == FinalDecision.ASSIGNED
                                            )
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

                            User user =
                                    userRepository.findById(a.getUserId())
                                            .orElseThrow();

                            Employee employee =
                                    employeeRepository.findById(a.getUserId())
                                            .orElseThrow();

                            String jobName =
                                    jobNameMap.get(employee.getJob().getJobId());

                            String mainSkill = resolveMainSkill(employee);

                            WorkStatus workStatus =
                                    a.getFinalDecision() == FinalDecision.ASSIGNED
                                            ? WorkStatus.ASSIGNED
                                            : WorkStatus.AVAILABLE;

                            return new AssignmentCandidateItemDTO(
                                    user.getUserId(),
                                    user.getUserName(),
                                    jobName,
                                    mainSkill,
                                    employee.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    null,                          // 적합도 점수 (Query에선 계산 X)
                                    a.getFinalDecision() == FinalDecision.ASSIGNED
                            );
                        })
                        .toList();

        return new AssignmentCandidatesView(jobSummaries, candidates);
    }
}


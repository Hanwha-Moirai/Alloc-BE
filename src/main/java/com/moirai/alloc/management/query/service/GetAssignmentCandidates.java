package com.moirai.alloc.management.query.service;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.CandidateSelectionService;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScore;
import com.moirai.alloc.management.domain.policy.scoring.CandidateScoringService;
import com.moirai.alloc.management.domain.policy.scoring.ScoreWeight;
import com.moirai.alloc.management.domain.policy.scoring.WeightPolicy;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.candidateList.CandidateScoreFilter;
import com.moirai.alloc.management.query.dto.candidateList.JobAssignmentSummaryDTO;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.policy.ScoreWeightAdjuster;
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
    private final CandidateScoringService candidateScoringService;
    private final WeightPolicy weightPolicy;
    private final ScoreWeightAdjuster scoreWeightAdjuster;

    //최초 / 전체 후보 조회
    public AssignmentCandidatePageView getAssignmentCandidates(
            Long projectId,
            CandidateScoreFilter filter
    ) {
        Project project = findProject(projectId);

        Map<Long, Integer> requiredCountByJobId =
                buildFullRequiredCount(project);

        return buildPage(project, requiredCountByJobId, filter);
    }

    //추가 / 재선발 후보 조회 (shortage 기준)

    public AssignmentCandidatePageView getAdditionalAssignmentCandidates(
            Long projectId,
            CandidateScoreFilter filter
    ) {
        Project project = findProject(projectId);

        Map<Long, Integer> requiredCountByJobId =
                buildShortageRequiredCount(project);

        return buildPage(project, requiredCountByJobId, filter);
    }

    // =========================
    // 공통 페이지 구성 로직
    // =========================
    private AssignmentCandidatePageView buildPage(
            Project project,
            Map<Long, Integer> requiredCountByJobId,
            CandidateScoreFilter filter
    ) {
        Long projectId = project.getProjectId();

        // 1) 정책 기반 후보 추천
        AssignCandidateDTO recommended =
                candidateSelectionService.select(project, requiredCountByJobId);

        // 2) 현재까지 배정된 인원
        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        // 3) employee 조회
        Set<Long> userIds =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .map(ScoredCandidateDTO::getUserId)
                        .collect(Collectors.toSet());

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(
                                Employee::getUserId,
                                Function.identity()
                        ));

        // 4) jobId → jobName
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

        // 5) 직군 요약
        List<JobAssignmentSummaryDTO> jobSummaries =
                project.getJobRequirements().stream()
                        .map(req -> createJobSummary(
                                req,
                                assignments,
                                employeeMap,
                                jobNameMap.get(req.getJobId())
                        ))
                        .toList();

        // 6) 근무 중 인원
        Set<Long> workingUserIds =
                assignmentRepository.findUserIdsByFinalDecision(FinalDecision.ASSIGNED);

        // 7) 점수 가중치
        ScoreWeight baseWeight = weightPolicy.getBaseWeight(project);
        ScoreWeight adjustedWeight = scoreWeightAdjuster.adjust(baseWeight, filter);

        Map<Long, Integer> weightedScoreMap = new HashMap<>();

        List<AssignmentCandidateItemDTO> candidates =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .map(c -> {
                            Employee e = employeeMap.get(c.getUserId());
                            if (e == null) return null;

                            User u = e.getUser();

                            AssignmentCandidateItemDTO.WorkStatus workStatus =
                                    workingUserIds.contains(u.getUserId())
                                            ? AssignmentCandidateItemDTO.WorkStatus.ASSIGNED
                                            : AssignmentCandidateItemDTO.WorkStatus.AVAILABLE;

                            CandidateScore raw =
                                    candidateScoringService.score(project, e);

                            int weightedScore =
                                    weightPolicy.apply(raw, adjustedWeight);

                            weightedScoreMap.put(u.getUserId(), weightedScore);

                            return new AssignmentCandidateItemDTO(
                                    u.getUserId(),
                                    u.getUserName(),
                                    e.getJob().getJobId(),
                                    e.getJob().getJobName(),
                                    resolveMainSkill(e),
                                    e.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    raw.getSkillScore(),
                                    raw.getExperienceScore(),
                                    raw.getAvailabilityScore(),
                                    false
                            );
                        })
                        .filter(Objects::nonNull)
                        .sorted(
                                Comparator.comparingInt(
                                        (AssignmentCandidateItemDTO dto) ->
                                                weightedScoreMap.getOrDefault(dto.getUserId(), 0)
                                ).reversed()
                        )
                        .toList();

        return new AssignmentCandidatePageView(jobSummaries, candidates);
    }

    // =========================
    // requiredCount 계산
    // =========================
    private Map<Long, Integer> buildFullRequiredCount(Project project) {
        return project.getJobRequirements().stream()
                .collect(Collectors.toMap(
                        JobRequirement::getJobId,
                        JobRequirement::getRequiredCount
                ));
    }

    private Map<Long, Integer> buildShortageRequiredCount(Project project) {

        Long projectId = project.getProjectId();
        Map<Long, Integer> result = new HashMap<>();

        for (JobRequirement req : project.getJobRequirements()) {

            long assignedCount =
                    assignmentRepository.countAssignedByProjectAndJob(
                            projectId,
                            req.getJobId()
                    );

            int shortage =
                    req.getRequiredCount() - (int) assignedCount;

            if (shortage > 0) {
                result.put(req.getJobId(), shortage);
            }
        }

        return result;
    }

    // =========================
    // 헬퍼
    // =========================
    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

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
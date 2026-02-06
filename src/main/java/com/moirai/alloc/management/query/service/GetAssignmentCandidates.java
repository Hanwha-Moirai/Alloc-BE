package com.moirai.alloc.management.query.service;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.AssignmentShortageCalculator;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
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
    private final AssignmentShortageCalculator shortageCalculator;

    public AssignmentCandidatePageView getAssignmentCandidates(
            Long projectId,
            CandidateScoreFilter filter
    ) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        Map<Long, Integer> shortageMap = shortageCalculator.calculate(project);

        Map<Long, Integer> requiredCountByJobId;
        if (shortageMap != null && !shortageMap.isEmpty()) {
            requiredCountByJobId = shortageMap; // 추가 선발용
            log.info("[GetAssignmentCandidates] projectId={} additionalMode=TRUE shortageMap={}", projectId, shortageMap);
        } else {
            requiredCountByJobId = project.getJobRequirements().stream()
                    .collect(Collectors.toMap(
                            JobRequirement::getJobId,
                            JobRequirement::getRequiredCount
                    )); // 최초 선발용
            log.info("[GetAssignmentCandidates] projectId={} additionalMode=FALSE requiredMap={}", projectId, requiredCountByJobId);
        }


        AssignCandidateDTO recommended =
                candidateSelectionService.select(project, requiredCountByJobId);

        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        Set<Long> recommendedUserIds =
                recommended.getAssignments().stream()
                        .flatMap(a -> a.getCandidates().stream())
                        .map(ScoredCandidateDTO::getUserId)
                        .collect(Collectors.toSet());

        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(recommendedUserIds);

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllById(recommendedUserIds).stream()
                        .collect(Collectors.toMap(
                                Employee::getUserId,
                                Function.identity()
                        ));

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

        List<JobAssignmentSummaryDTO> jobSummaries =
                project.getJobRequirements().stream()
                        .map(req -> createJobSummary(
                                req,
                                assignments,
                                employeeMap,
                                jobNameMap.get(req.getJobId())
                        ))
                        .toList();

        Set<Long> workingUserIds =
                assignmentRepository.findUserIdsByFinalDecision(
                        FinalDecision.ASSIGNED
                );

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

                            CandidateScore rawScore =
                                    candidateScoringService.score(project, e);

                            int weightedScore =
                                    weightPolicy.apply(rawScore, adjustedWeight);

                            weightedScoreMap.put(u.getUserId(), weightedScore);

                            return new AssignmentCandidateItemDTO(
                                    u.getUserId(),
                                    u.getUserName(),
                                    e.getJob().getJobId(),
                                    e.getJob().getJobName(),
                                    resolveMainSkill(e),
                                    e.getTitleStandard().getMonthlyCost(),
                                    workStatus,
                                    rawScore.getSkillScore(),
                                    rawScore.getExperienceScore(),
                                    rawScore.getAvailabilityScore(),
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
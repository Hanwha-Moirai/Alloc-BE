package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.policy.AssignmentShortageCalculator;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.project.command.domain.Project;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SelectAssignmentCandidates {
//        1) projectId로 프로젝트를 조회한다
//        2) policy 기반 후보 리스트를 조회하고 사용자가 선택한다
//        3) 선택 결과가 직군별 requiredCount를 충족하는지 검증한다
//        4) 검증된 선택 결과를 배정 후보로 저장한다


    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final GetAssignmentCandidates getAssignmentCandidates;
    private final AssignmentShortageCalculator shortageCalculator;

    public SelectAssignmentCandidates(
            SquadAssignmentRepository assignmentRepository,
            ProjectRepository projectRepository,
            ApplicationEventPublisher eventPublisher,
            GetAssignmentCandidates getAssignmentCandidates,
            AssignmentShortageCalculator shortageCalculator
    ) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.eventPublisher = eventPublisher;
        this.getAssignmentCandidates = getAssignmentCandidates;
        this.shortageCalculator = shortageCalculator;
    }

    public void selectAssignmentCandidates(AssignCandidateDTO command) {

        if (command == null) {
            log.warn("selectasssignmentcandidates called with null command");
            return;
        }
        // 1) 프로젝트 조회
        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<JobAssignmentDTO> commandAssignments =
                command.getAssignments() == null ? Collections.emptyList() : command.getAssignments();

        // 2) 직군별 선택 인원 검증 (정확히 requiredCount만큼 선택했는지)
        validateSelectedCounts(project, command);

        // 3) 신규 후보 생성
        for (JobAssignmentDTO assignment : command.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {

                if (candidate == null || candidate.getUserId() == null) continue;

                Long userId = candidate.getUserId();

                if (assignmentRepository.existsByProjectIdAndUserId(
                        project.getProjectId(), userId)) {
                    continue;
                }

                SquadAssignment saved = assignmentRepository.save(
                        SquadAssignment.propose(
                                project.getProjectId(),
                                userId,
                                candidate.getFitnessScore()
                        )
                );
                ProjectTempAssignmentEvent event = new ProjectTempAssignmentEvent(
                        project.getProjectId(),
                        project.getName(),
                        saved.getUserId()
                );
                eventPublisher.publishEvent(event);
                log.info("Published ProjectTempAssignmentEvent projectId={} userId={} projectName={}",
                        event.projectId(), event.userId(), event.projectName());
            }
        }
    }
    /**
     * 프론트 전용 Command 진입점
     * userIds → AssignCandidateDTO 재구성
     */
    public void selectByUserIds(Long projectId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("selectByUserIds called with empty userIds projectId={}", projectId);
            return;
        }
        // 1) 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2) 현재 후보 조회
        AssignmentCandidatePageView page =
                getAssignmentCandidates.getAssignmentCandidates(projectId, null);

        if (page == null || page.getCandidates() == null) {
            log.warn("selectByUserIds page or candidates is null projectId={}", projectId);
            return;
        }

        // 3) userId 기준 필터 + jobId 기준 그룹핑
        Map<Long, List<ScoredCandidateDTO>> groupedByJob =
                page.getCandidates().stream()
                        .filter(item -> item != null)
                        .filter(item -> userIds.contains(item.getUserId()))
                        .collect(Collectors.groupingBy(
                                AssignmentCandidateItemDTO::getJobId,
                                Collectors.mapping(
                                        item -> new ScoredCandidateDTO(
                                                item.getUserId(),
                                                item.getSkillScore()
                                                        + item.getExperienceScore()
                                                        + item.getAvailabilityScore()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        // 4) 내부 Command DTO로 변환
        List<JobAssignmentDTO> assignments =
                groupedByJob.entrySet().stream()
                        .map(e -> new JobAssignmentDTO(
                                e.getKey(),
                                e.getValue()
                        ))
                        .toList();

        // 5) 기존 로직 재사용
        selectAssignmentCandidates(
                new AssignCandidateDTO(projectId, assignments)
        );
    }
    private void validateSelectedCounts(
            Project project,
            AssignCandidateDTO command
    ) {
        List<JobAssignmentDTO> commandAssignments =
                command.getAssignments() == null ? Collections.emptyList() : command.getAssignments();

        Map<Long, JobAssignmentDTO> selectionMap =
                commandAssignments.stream()
                        .filter(a -> a != null)
                        .collect(Collectors.toMap(
                                JobAssignmentDTO::getJobId,
                                Function.identity(),
                                (a, b) -> a
                        ));

        Map<Long, Integer> shortageMap =
                shortageCalculator.calculate(project);

        boolean isAdditionalSelection = !shortageMap.isEmpty();

        for (JobRequirement requirement : project.getJobRequirements()) {

            Long jobId = requirement.getJobId();

            JobAssignmentDTO selection = selectionMap.get(jobId);

            int actual =
                    selection == null || selection.getCandidates() == null
                            ? 0
                            : selection.getCandidates().size();

            int expected = isAdditionalSelection
                    ? shortageMap.getOrDefault(jobId, 0)
                    : requirement.getRequiredCount();


            if (actual != expected) {
                throw new IllegalArgumentException(
                        "Must select exactly "
                                + expected
                                + " candidates for jobId="
                                + jobId
                );
            }
            log.info("ShortageMap = {}", shortageMap);
            log.info("jobId={} expected={} actual={}", jobId, expected, actual);
        }

    }
}

package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.command.dto.AssignCandidateDTO;
import com.moirai.alloc.management.command.dto.JobAssignmentDTO;
import com.moirai.alloc.management.command.dto.ScoredCandidateDTO;
import com.moirai.alloc.management.command.event.ProjectTempAssignmentEvent;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.domain.vo.JobRequirement;
import com.moirai.alloc.management.query.dto.candidateList.AssignmentCandidateItemDTO;
import com.moirai.alloc.management.query.dto.controllerDto.AssignmentCandidatePageView;
import com.moirai.alloc.management.query.service.GetAssignmentCandidates;
import com.moirai.alloc.project.command.domain.Project;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SelectAssignmentCandidates {
//        1) projectId로 프로젝트를 조회한다
//        2) policy 기반 후보 리스트를 조회하고 사용자가 선택한다
//        3) 선택 결과가 직군별 requiredCount를 충족하는지 검증한다
//        4) 검증된 선택 결과를 배정 후보로 저장한다


    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final GetAssignmentCandidates getAssignmentCandidates;
    private final ApplicationEventPublisher eventPublisher;

    public SelectAssignmentCandidates(
            SquadAssignmentRepository assignmentRepository,
            ProjectRepository projectRepository,
            GetAssignmentCandidates getAssignmentCandidates,
            ApplicationEventPublisher eventPublisher
    ) {
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.getAssignmentCandidates = getAssignmentCandidates;
        this.eventPublisher = eventPublisher;
    }

    public void selectAssignmentCandidates(AssignCandidateDTO command) {

        // 1) 프로젝트 조회
        Project project = projectRepository.findById(command.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2) 직군별 선택 인원 검증 (정확히 requiredCount만큼 선택했는지)
        validateSelectedCounts(project, command);

        // 3) 신규 후보 생성
        for (JobAssignmentDTO assignment : command.getAssignments()) {
            for (ScoredCandidateDTO candidate : assignment.getCandidates()) {
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
                eventPublisher.publishEvent(new ProjectTempAssignmentEvent(
                        project.getProjectId(),
                        project.getName(),
                        saved.getUserId()
                ));
            }
        }
    }
    /**
     * 🆕 프론트 전용 Command 진입점
     * userIds → AssignCandidateDTO 재구성
     */
    public void selectByUserIds(Long projectId, List<Long> userIds) {

        // 1) 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // 2) 현재 후보 조회
        AssignmentCandidatePageView page =
                getAssignmentCandidates.getAssignmentCandidates(projectId, null);

        // 3) userId 기준 필터 + jobId 기준 그룹핑 (⭐ 핵심)
        Map<Long, List<ScoredCandidateDTO>> groupedByJob =
                page.getCandidates().stream()
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

    //직군별로 requiredCount를 정확히 충족했는지 검증
    private void validateSelectedCounts(
            Project project,
            AssignCandidateDTO command
    ) {
        Map<Long, JobAssignmentDTO> selectionMap =
                command.getAssignments().stream()
                        .collect(Collectors.toMap(
                                JobAssignmentDTO::getJobId,
                                Function.identity()
                        ));

        for (JobRequirement requirement : project.getJobRequirements()) {

            JobAssignmentDTO selection =
                    selectionMap.get(requirement.getJobId());

            if (selection == null) {
                throw new IllegalArgumentException(
                        "No candidates selected for jobId=" + requirement.getJobId()
                );
            }

            if (selection.getCandidates().size()
                    != requirement.getRequiredCount()) {
                throw new IllegalArgumentException(
                        "Must select exactly "
                                + requirement.getRequiredCount()
                                + " candidates for jobId="
                                + requirement.getJobId()
                );
            }
        }
    }
}

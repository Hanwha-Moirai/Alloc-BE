package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.ProjectRepository;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.userAssign.MyPendingAssignmentDTO;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyPendingAssignments {
    private final SquadAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;

    public List<MyPendingAssignmentDTO> getMyPendingAssignments(Long userId) {

        List<SquadAssignment> assignments =
                assignmentRepository.findByUserIdAndAssignmentStatus(
                        userId,
                        AssignmentStatus.REQUESTED
                );

        return assignments.stream()
                .map(a -> {
                    Project project = projectRepository.findById(a.getProjectId())
                            .orElseThrow();

                    return new MyPendingAssignmentDTO(
                            a.getAssignmentId(),
                            a.getProjectId(),
                            project.getName(),
                            a.getAssignmentStatus()
                    );
                })
                .toList();
    }
}

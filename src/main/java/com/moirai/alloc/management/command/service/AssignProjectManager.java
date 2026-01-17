package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignProjectManager {
    private final SquadAssignmentRepository squadAssignmentRepository;

    public void assignPm(Long projectId, Long pmUserId) {

        SquadAssignment assignment =
                SquadAssignment.assignPm(
                        projectId,
                        pmUserId
                );

        squadAssignmentRepository.save(assignment);
    }
}

package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.management.domain.FinalDecision;
import com.moirai.alloc.management.domain.SquadAssignment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SquadAssignmentRepository extends CrudRepository<SquadAssignment, Long> {
    boolean existsByProjectIdAndUserIdAndFinalDecision(Long projectId, Long aLong, FinalDecision finalDecision);

    List<SquadAssignment> findByProjectIdAndFinalDecision(Long projectId, FinalDecision finalDecision);
}

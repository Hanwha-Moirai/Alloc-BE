package com.moirai.alloc.gantt.command.domain.repository;

import com.moirai.alloc.gantt.command.domain.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;



public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    Optional<Milestone> findByMilestoneIdAndProjectId(Long milestoneId, Long projectId);
}

package com.moirai.alloc.gantt.command.domain.repository;

import com.moirai.alloc.gantt.command.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByMilestone_MilestoneId(Long milestoneId);

    boolean existsByMilestone_MilestoneIdAndIsDeletedFalse(Long milestoneId);
}

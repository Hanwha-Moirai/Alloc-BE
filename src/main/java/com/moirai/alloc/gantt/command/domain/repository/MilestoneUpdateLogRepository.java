package com.moirai.alloc.gantt.command.domain.repository;

import com.moirai.alloc.gantt.command.domain.entity.MilestoneUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneUpdateLogRepository extends JpaRepository<MilestoneUpdateLog, Long> {
}

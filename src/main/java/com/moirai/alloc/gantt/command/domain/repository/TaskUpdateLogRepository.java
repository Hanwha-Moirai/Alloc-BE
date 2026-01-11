package com.moirai.alloc.gantt.command.domain.repository;

import com.moirai.alloc.gantt.command.domain.entity.TaskUpdateLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskUpdateLogRepository extends JpaRepository<TaskUpdateLog, Long> {
}

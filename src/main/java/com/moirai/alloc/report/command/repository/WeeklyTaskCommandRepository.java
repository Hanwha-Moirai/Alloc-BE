package com.moirai.alloc.report.command.repository;

import com.moirai.alloc.report.command.domain.entity.WeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyTaskCommandRepository extends JpaRepository<WeeklyTask, Long> {
    void deleteByReportReportId(Long reportId);
}

package com.moirai.alloc.report.command.repository;

import com.moirai.alloc.report.command.domain.entity.WeeklyReportLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyReportLogRepository extends JpaRepository<WeeklyReportLog, Long> {
    List<WeeklyReportLog> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
}

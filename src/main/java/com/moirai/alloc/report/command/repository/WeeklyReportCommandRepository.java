package com.moirai.alloc.report.command.repository;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyReportCommandRepository extends JpaRepository<WeeklyReport, Long> {
    Optional<WeeklyReport> findByReportIdAndIsDeletedFalse(Long reportId);
}

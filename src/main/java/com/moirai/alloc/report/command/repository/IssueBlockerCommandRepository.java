package com.moirai.alloc.report.command.repository;

import com.moirai.alloc.report.command.domain.entity.IssueBlocker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueBlockerCommandRepository extends JpaRepository<IssueBlocker, Long> {
    void deleteByWeeklyTaskReportReportId(Long reportId);
}

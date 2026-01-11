package com.moirai.alloc.report.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "weekly_report")
public class WeeklyReport extends BaseTimeEntity {

    public enum ReportStatus { DRAFT, REVIEWED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus = ReportStatus.DRAFT;

    @Lob
    @Column(name = "change_of_plan")
    private String changeOfPlan;

    @Lob
    @Column(name = "summary_text")
    private String summaryText;

    @Column(name = "task_completion_rate", nullable = false)
    private Double taskCompletionRate;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    public static WeeklyReport create(Long userId,
                                      Long projectId,
                                      LocalDate weekStartDate,
                                      LocalDate weekEndDate) {
        return WeeklyReport.builder()
                .userId(userId)
                .projectId(projectId)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .reportStatus(ReportStatus.DRAFT)
                .taskCompletionRate(0.0)
                .isDeleted(false)
                .build();
    }

    public void updateReport(ReportStatus reportStatus,
                             String changeOfPlan,
                             Double taskCompletionRate) {
        if (reportStatus != null) {
            this.reportStatus = reportStatus;
        }
        if (changeOfPlan != null) {
            this.changeOfPlan = changeOfPlan;
        }
        if (taskCompletionRate != null) {
            this.taskCompletionRate = taskCompletionRate;
        }
    }

    public void markDeleted() {
        this.isDeleted = true;
    }

    @Builder
    private WeeklyReport(Long userId,
                         Long projectId,
                         LocalDate weekStartDate,
                         LocalDate weekEndDate,
                         ReportStatus reportStatus,
                         String changeOfPlan,
                         String summaryText,
                         Double taskCompletionRate,
                         Boolean isDeleted) {
        this.userId = userId;
        this.projectId = projectId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.reportStatus = (reportStatus == null) ? ReportStatus.DRAFT : reportStatus;
        this.changeOfPlan = changeOfPlan;
        this.summaryText = summaryText;
        this.taskCompletionRate = taskCompletionRate;
        this.isDeleted = (isDeleted == null) ? false : isDeleted;
    }
}

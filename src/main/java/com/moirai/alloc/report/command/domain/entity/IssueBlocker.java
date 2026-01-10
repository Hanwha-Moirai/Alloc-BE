package com.moirai.alloc.report.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "issue_blockers")
public class IssueBlocker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_blockers_id")
    private Long issueBlockersId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_tasks_id", nullable = false)
    private WeeklyTask weeklyTask;

    @Lob
    @Column(name = "cause_of_delay")
    private String causeOfDelay;

    @Lob
    @Column(name = "dependency_summary")
    private String dependencySummary;

    @Column(name = "delayed_dates")
    private Integer delayedDates;

    @Builder
    private IssueBlocker(WeeklyTask weeklyTask,
                         String causeOfDelay,
                         String dependencySummary,
                         Integer delayedDates) {
        this.weeklyTask = weeklyTask;
        this.causeOfDelay = causeOfDelay;
        this.dependencySummary = dependencySummary;
        this.delayedDates = delayedDates;
    }
}

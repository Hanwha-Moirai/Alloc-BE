package com.moirai.alloc.report.command.domain.entity;

import com.moirai.alloc.gantt.command.domain.entity.Task;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "weekly_tasks")
public class WeeklyTask {

    public enum TaskType { COMPLETED, INCOMPLETE, NEXT_WEEK }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_tasks_id")
    private Long weeklyTasksId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private WeeklyReport report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "planned_start_date")
    private java.time.LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private java.time.LocalDate plannedEndDate;

    public static WeeklyTask create(WeeklyReport report,
                                    Task task,
                                    TaskType taskType,
                                    java.time.LocalDate plannedStartDate,
                                    java.time.LocalDate plannedEndDate) {
        return WeeklyTask.builder()
                .report(report)
                .task(task)
                .taskType(taskType)
                .plannedStartDate(plannedStartDate)
                .plannedEndDate(plannedEndDate)
                .build();
    }

    @Builder
    private WeeklyTask(WeeklyReport report,
                       Task task,
                       TaskType taskType,
                       java.time.LocalDate plannedStartDate,
                       java.time.LocalDate plannedEndDate) {
        this.report = report;
        this.task = task;
        this.taskType = taskType;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
    }
}

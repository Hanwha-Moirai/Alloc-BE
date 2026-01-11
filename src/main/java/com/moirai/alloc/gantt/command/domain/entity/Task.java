package com.moirai.alloc.gantt.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "task")
public class Task extends BaseTimeEntity {

    public enum TaskCategory { DEVELOPMENT, TESTING, BUGFIXING, DISTRIBUTION }
    public enum TaskStatus { TODO, INPROGRESS, DONE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_category")
    private TaskCategory taskCategory = TaskCategory.DEVELOPMENT;

    @Column(name = "task_name", nullable = false, length = 150)
    private String taskName;

    @Lob
    @Column(name = "task_description", nullable = false)
    private String taskDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_status")
    private TaskStatus taskStatus = TaskStatus.TODO;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private Task(Milestone milestone,
                 Long userId,
                 TaskCategory taskCategory,
                 String taskName,
                 String taskDescription,
                 TaskStatus taskStatus,
                 LocalDate startDate,
                 LocalDate endDate,
                 Boolean isCompleted,
                 Boolean isDeleted) {
        this.milestone = milestone;
        this.userId = userId;
        this.taskCategory = (taskCategory == null) ? TaskCategory.DEVELOPMENT : taskCategory;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = (taskStatus == null) ? TaskStatus.TODO : taskStatus;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = (isCompleted == null) ? false : isCompleted;
        this.isDeleted = (isDeleted == null) ? false : isDeleted;
    }

    public void markCompleted() {
        this.taskStatus = TaskStatus.DONE;
        this.isCompleted = true;
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void updateTask(Milestone milestone,
                           Long userId,
                           TaskCategory taskCategory,
                           String taskName,
                           String taskDescription,
                           TaskStatus taskStatus,
                           LocalDate startDate,
                           LocalDate endDate) {
        this.milestone = milestone;
        this.userId = userId;
        this.taskCategory = taskCategory;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

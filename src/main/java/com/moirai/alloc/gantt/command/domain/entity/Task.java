package com.moirai.alloc.gantt.command.domain.entity;

import com.moirai.alloc.user.command.domain.User;
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
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "task")
public class Task {

    public enum TaskCategory { DEVELOPMENT, TESTING, BUGFIXING, DISTRIBUTION }
    public enum TaskStatus { TODO, INPROGRESS, DONE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestone milestone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "is_deleted", nullable = false, length = 255)
    private String isDeleted = "FALSE";

    @Builder
    private Task(Milestone milestone,
                 User user,
                 TaskCategory taskCategory,
                 String taskName,
                 String taskDescription,
                 TaskStatus taskStatus,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt,
                 LocalDate startDate,
                 LocalDate endDate,
                 Boolean isCompleted,
                 String isDeleted) {
        this.milestone = milestone;
        this.user = user;
        this.taskCategory = (taskCategory == null) ? TaskCategory.DEVELOPMENT : taskCategory;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = (taskStatus == null) ? TaskStatus.TODO : taskStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCompleted = (isCompleted == null) ? false : isCompleted;
        this.isDeleted = (isDeleted == null) ? "FALSE" : isDeleted;
    }
}

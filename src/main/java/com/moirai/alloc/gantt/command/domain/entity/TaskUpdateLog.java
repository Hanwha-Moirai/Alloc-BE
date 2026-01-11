package com.moirai.alloc.gantt.command.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "task_update_log")
public class TaskUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_update_log_id")
    private Long taskUpdateLogId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_reason")
    private String updateReason;

    public static TaskUpdateLog create(Long taskId, String updateReason) {
        TaskUpdateLog log = new TaskUpdateLog();
        log.taskId = taskId;
        log.updateReason = updateReason;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}

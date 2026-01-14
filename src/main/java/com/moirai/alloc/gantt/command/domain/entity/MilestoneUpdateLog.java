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
@Table(name = "milestone_update_log")
public class MilestoneUpdateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_update_log_id")
    private Long milestoneUpdateLogId;

    @Column(name = "milestone_id", nullable = false)
    private Long milestoneId;

    @Column(name = "update_reason")
    private String updateReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static MilestoneUpdateLog create(Long milestoneId, String updateReason) {
        MilestoneUpdateLog log = new MilestoneUpdateLog();
        log.milestoneId = milestoneId;
        log.updateReason = updateReason;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}

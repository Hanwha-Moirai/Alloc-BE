package com.moirai.alloc.report.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "weekly_report_log")
public class WeeklyReportLog extends BaseTimeEntity {

    public enum ActionType { CREATE, UPDATE, DELETE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_log_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType actionType;

    @Column(name = "log_message", nullable = false, length = 255)
    private String logMessage;

    @Builder
    private WeeklyReportLog(
            Long projectId,
            Long reportId,
            Long actorUserId,
            ActionType actionType,
            String logMessage
    ) {
        this.projectId = Objects.requireNonNull(projectId, "projectId");
        this.reportId = Objects.requireNonNull(reportId, "reportId");
        this.actorUserId = Objects.requireNonNull(actorUserId, "actorUserId");
        this.actionType = Objects.requireNonNull(actionType, "actionType");
        this.logMessage = Objects.requireNonNull(logMessage, "logMessage");
    }
}

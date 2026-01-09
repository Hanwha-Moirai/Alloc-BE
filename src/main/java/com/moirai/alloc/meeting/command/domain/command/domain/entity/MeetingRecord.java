package com.moirai.alloc.meeting.command.domain.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import com.moirai.alloc.project.command.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "meeting_record")
public class MeetingRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long meetingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "created_by", length = 40)
    private String createdBy;

    @Column(name = "progress")
    private Double progress;

    @Column(name = "meeting_date")
    private LocalDateTime meetingDate;

    @Column(name = "meeting_time")
    private LocalDateTime meetingTime;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private MeetingRecord(Project project,
                          String createdBy,
                          Double progress,
                          LocalDateTime meetingDate,
                          LocalDateTime meetingTime,
                          Boolean isDeleted) {
        this.project = project;
        this.createdBy = createdBy;
        this.progress = progress;
        this.meetingDate = meetingDate;
        this.meetingTime = meetingTime;
        this.isDeleted = (isDeleted == null) ? false : isDeleted;
    }
}

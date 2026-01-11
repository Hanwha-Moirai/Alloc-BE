package com.moirai.alloc.gantt.command.domain.entity;

import com.moirai.alloc.common.model.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "milestone")
public class Milestone extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "milestone_id")
    private Long milestoneId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "milestone_name", nullable = false, length = 150)
    private String milestoneName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "achievement_rate")
    private Long achievementRate;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private Milestone(Long projectId,
                      String milestoneName,
                      LocalDate startDate,
                      LocalDate endDate,
                      Long achievementRate,
                      Boolean isDeleted) {
        this.projectId = projectId;
        this.milestoneName = milestoneName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.achievementRate = achievementRate;
        this.isDeleted = (isDeleted == null) ? false : isDeleted;
    }

    public void changeName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public void changeSchedule(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void changeAchievementRate(Long achievementRate) {
        this.achievementRate = achievementRate;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}

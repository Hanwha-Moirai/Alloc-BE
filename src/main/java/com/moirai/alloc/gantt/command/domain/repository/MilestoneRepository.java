package com.moirai.alloc.gantt.command.domain.repository;

import com.moirai.alloc.calendar.query.dto.MilestoneCalendarRow;
import com.moirai.alloc.gantt.command.domain.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;


public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    Optional<Milestone> findByMilestoneIdAndProjectId(Long milestoneId, Long projectId);

    @Query("""
        select new com.moirai.alloc.calendar.query.dto.MilestoneCalendarRow(
            m.milestoneId,
            m.milestoneName,
            m.startDate,
            m.endDate,
            m.achievementRate
        )
        from Milestone m
        where m.projectId = :projectId
          and coalesce(m.isDeleted, false) = false
          and m.startDate <= :to
          and m.endDate >= :from
        """)
    List<MilestoneCalendarRow> findCalendarMilestones(Long projectId, LocalDate from, LocalDate to);
}

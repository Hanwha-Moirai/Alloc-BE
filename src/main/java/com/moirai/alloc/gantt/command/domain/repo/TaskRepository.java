package com.moirai.alloc.gantt.command.domain.repo;

import com.moirai.alloc.calendar.query.dto.TaskCalendarRow;
import com.moirai.alloc.gantt.command.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
        select new com.moirai.alloc.calendar.query.dto.TaskCalendarRow(
            t.taskId,
            t.taskName,
            t.startDate,
            t.endDate,
            t.taskStatus,
            t.taskCategory,
            t.userId,
            m.milestoneId
        )
        from Task t
        join t.milestone m
        where m.projectId = :projectId
          and coalesce(m.isDeleted, false) = false
          and coalesce(t.isDeleted, false) = false
          and t.startDate <= :to
          and t.endDate >= :from
        """)
    List<TaskCalendarRow> findCalendarTasks(Long projectId, LocalDate from, LocalDate to);
}

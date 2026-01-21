package com.moirai.alloc.gantt.query.mapper;

import com.moirai.alloc.gantt.query.dto.projection.TaskProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TaskQueryMapper {
    List<TaskProjection> findTasks(
            @Param("projectId") Long projectId,
            @Param("assigneeNames") List<String> assigneeNames,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("taskCategories") List<String> taskCategories
    );

    TaskProjection findTaskById(
            @Param("projectId") Long projectId,
            @Param("taskId") Long taskId
    );

    List<TaskProjection> findTasksByMilestone(
            @Param("projectId") Long projectId,
            @Param("milestoneId") Long milestoneId
    );
}

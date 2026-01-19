package com.moirai.alloc.gantt.query.mapper;

import com.moirai.alloc.gantt.query.dto.projection.MilestoneProjection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MilestoneQueryMapper {
    List<MilestoneProjection> findMilestones(@Param("projectId") Long projectId);

    List<Boolean> findMilestoneCompletionStates(@Param("projectId") Long projectId);
}

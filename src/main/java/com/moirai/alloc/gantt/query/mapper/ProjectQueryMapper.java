package com.moirai.alloc.gantt.query.mapper;

import com.moirai.alloc.common.port.ProjectPeriod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectQueryMapper {
    ProjectPeriod findProjectPeriod(@Param("projectId") Long projectId);
}

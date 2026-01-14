package com.moirai.alloc.gantt.query.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMembershipQueryMapper {
    boolean existsAssignedMember(@Param("projectId") Long projectId, @Param("userId") Long userId);
}

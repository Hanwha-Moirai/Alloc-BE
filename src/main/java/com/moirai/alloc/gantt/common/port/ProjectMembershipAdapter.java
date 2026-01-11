package com.moirai.alloc.gantt.common.port;

import com.moirai.alloc.common.port.ProjectMembershipPort;
import com.moirai.alloc.gantt.query.mapper.ProjectMembershipQueryMapper;
import org.springframework.stereotype.Component;

@Component
public class ProjectMembershipAdapter implements ProjectMembershipPort {

    private final ProjectMembershipQueryMapper projectMembershipQueryMapper;

    public ProjectMembershipAdapter(ProjectMembershipQueryMapper projectMembershipQueryMapper) {
        this.projectMembershipQueryMapper = projectMembershipQueryMapper;
    }

    @Override
    public boolean isMember(Long projectId, Long userId) {
        return projectMembershipQueryMapper.existsAssignedMember(projectId, userId);
    }

    @Override
    public boolean isPm(Long projectId, Long userId) {
        return isMember(projectId, userId);
    }
}

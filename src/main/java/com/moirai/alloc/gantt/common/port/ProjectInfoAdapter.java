package com.moirai.alloc.gantt.common.port;

import com.moirai.alloc.common.port.ProjectInfoPort;
import com.moirai.alloc.common.port.ProjectPeriod;
import com.moirai.alloc.gantt.query.mapper.ProjectQueryMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProjectInfoAdapter implements ProjectInfoPort {

    private final ProjectQueryMapper projectQueryMapper;

    public ProjectInfoAdapter(ProjectQueryMapper projectQueryMapper) {
        this.projectQueryMapper = projectQueryMapper;
    }

    @Override
    public Optional<ProjectPeriod> findProjectPeriod(Long projectId) {
        return Optional.ofNullable(projectQueryMapper.findProjectPeriod(projectId));
    }
}

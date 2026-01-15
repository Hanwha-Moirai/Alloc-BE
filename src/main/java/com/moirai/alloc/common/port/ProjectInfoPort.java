package com.moirai.alloc.common.port;

import java.util.Optional;

public interface ProjectInfoPort {
    Optional<ProjectPeriod> findProjectPeriod(Long projectId);
}

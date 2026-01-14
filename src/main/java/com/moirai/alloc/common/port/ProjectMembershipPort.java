package com.moirai.alloc.common.port;

public interface ProjectMembershipPort {
    boolean isMember(Long projectId, Long userId);
    boolean isPm(Long projectId, Long userId);
}

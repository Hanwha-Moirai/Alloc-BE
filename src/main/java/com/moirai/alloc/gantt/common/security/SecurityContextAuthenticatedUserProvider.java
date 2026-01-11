package com.moirai.alloc.gantt.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextAuthenticatedUserProvider implements AuthenticatedUserProvider {

    @Override
    public Long getCurrentUserId() {
        return 1L;
    }
}

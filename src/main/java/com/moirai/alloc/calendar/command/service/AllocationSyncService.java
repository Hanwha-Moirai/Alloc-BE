package com.moirai.alloc.calendar.command.service;

import com.moirai.alloc.calendar.command.dto.response.AllocationSyncResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;

public interface AllocationSyncService {
    AllocationSyncResponse syncToCalendar(Long projectId, UserPrincipal principal);
}

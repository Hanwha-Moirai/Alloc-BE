package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminUserListItem;
import com.moirai.alloc.common.dto.PageResponse;
import com.moirai.alloc.admin.query.mapper.AdminUserQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserQueryService {

    private final AdminUserQueryMapper mapper;

    public PageResponse<AdminUserListItem> getUsers(int page, int size, String q, String role) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<AdminUserListItem> content = mapper.selectUsers(safeSize, offset, q, role);
        long total = mapper.countUsers(q, role);

        return PageResponse.from(content, safePage, safeSize, (int) total);
    }
}

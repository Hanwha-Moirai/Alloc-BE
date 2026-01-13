package com.moirai.alloc.user.query.service;

import com.moirai.alloc.common.dto.PageResponse;
import com.moirai.alloc.user.query.dto.UserListItem;
import com.moirai.alloc.user.query.mapper.UserQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserQueryMapper mapper;

    public PageResponse<UserListItem> getUsers(int page, int size, String q, String role) {
        int safePage = Math.max(page, 1); // 1-based 유지
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        List<UserListItem> content = mapper.selectUsers(safeSize, offset, q, role);
        long total = mapper.countUsers(q, role);

        return PageResponse.from(content, safePage, safeSize, (int) total);
    }
}

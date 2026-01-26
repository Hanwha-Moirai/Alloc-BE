package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminJobListItem;
import com.moirai.alloc.admin.query.mapper.AdminJobQueryMapper;
import com.moirai.alloc.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminJobQueryService {

    private final AdminJobQueryMapper mapper;

    public PageResponse<AdminJobListItem> getJobs(int page, int size, String q) {

        int pageIndex = Math.max(page, 0);
        int pageSize  = Math.max(size, 1);
        int offset    = pageIndex * pageSize;

        List<AdminJobListItem> content = mapper.selectJobs(pageSize, offset, q);
        long total = mapper.countJobs(q);

        return PageResponse.from(content, pageIndex, pageSize, (int) total);
    }
}

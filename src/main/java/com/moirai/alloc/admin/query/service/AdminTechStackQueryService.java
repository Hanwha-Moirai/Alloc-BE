package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
import com.moirai.alloc.admin.query.mapper.AdminTechStackQueryMapper;
import com.moirai.alloc.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTechStackQueryService {

    private final AdminTechStackQueryMapper mapper;

    //기술 스택 조회
    public PageResponse<AdminTechStackListItem> getTechStacks(int page, int size, String q) {

        int pageNo = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (pageNo - 1) * pageSize;

        List<AdminTechStackListItem> content = mapper.selectTechStacks(pageSize, offset, q);
        long total = mapper.countTechStacks(q);

        return PageResponse.from(content, pageNo, pageSize, (int) total);
    }
}

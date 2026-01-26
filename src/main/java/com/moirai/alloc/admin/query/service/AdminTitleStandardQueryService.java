package com.moirai.alloc.admin.query.service;

import com.moirai.alloc.admin.query.dto.AdminTechStackListItem;
import com.moirai.alloc.admin.query.dto.AdminTitleStandardListItem;
import com.moirai.alloc.admin.query.mapper.AdminTitleStandardQueryMapper;
import com.moirai.alloc.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTitleStandardQueryService {

    private final AdminTitleStandardQueryMapper mapper;

    public PageResponse<AdminTitleStandardListItem> getTitleStandard(int page, int size, String q) {

        int pageIndex = Math.max(page, 0);
        int pageSize  = Math.max(size, 1);
        int offset    = pageIndex * pageSize;

        List<AdminTitleStandardListItem> content = mapper.selectTitleStandard(pageSize, offset, q);
        long total = mapper.countTitleStandard(q);

        return PageResponse.from(content, pageIndex, pageSize, (int) total);
    }
}

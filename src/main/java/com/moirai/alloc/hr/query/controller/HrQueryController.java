package com.moirai.alloc.hr.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.hr.query.dto.JobStandardResponse;
import com.moirai.alloc.hr.query.dto.TechStackDropdownResponse;
import com.moirai.alloc.hr.query.service.HrQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hr")
public class HrQueryController {

    private final HrQueryService hrQueryService;

    /* 직군 드롭다운 */
    @GetMapping("/jobs")
    public ApiResponse<List<JobStandardResponse>> getJobs() {
        return ApiResponse.success(hrQueryService.getJobs());
    }

    /* 기술스택 드롭다운(검색 + 스크롤) */
    @GetMapping("/tech-stacks")
    public ApiResponse<TechStackDropdownResponse> getTechStacks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cursorTechName,
            @RequestParam(required = false) Long cursorTechId,
            @RequestParam(required = false) Integer size
    ) {
        return ApiResponse.success(
                hrQueryService.getTechStacksDropdown(keyword, cursorTechName, cursorTechId, size)
        );
    }
}


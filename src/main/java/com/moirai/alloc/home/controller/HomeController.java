package com.moirai.alloc.home.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.home.query.dto.HomeProjectListItemDTO;
import com.moirai.alloc.home.query.dto.HomeProjectSummaryDTO;
import com.moirai.alloc.home.query.service.GetHomeProjectList;
import com.moirai.alloc.home.query.service.GetHomeProjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final GetHomeProjectList getHomeProjectList;
    private final GetHomeProjectSummary getHomeProjectSummary;

    //홈 - 프로젝트 목록 조회
    @GetMapping("/projects")
    @PreAuthorize("hasRole('USER') or hasRole('PM')")
    public List<HomeProjectListItemDTO> getHomeProjects(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return getHomeProjectList.getHomeProjectList(principal.userId());
    }


    //홈 - 프로젝트 상태 요약 (진행중/지연/종료)
    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER') or hasRole('PM')")
    public HomeProjectSummaryDTO getHomeSummary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return getHomeProjectSummary.getSummary(principal.userId());
    }
}
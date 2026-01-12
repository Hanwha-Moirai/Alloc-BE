package com.moirai.alloc.profile.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.profile.query.dto.MyProfileBasicResponse;
import com.moirai.alloc.profile.query.dto.MyProjectHistoryResponse;
import com.moirai.alloc.profile.query.dto.MyTechStackResponse;
import com.moirai.alloc.profile.query.service.MyProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class MyProfileQueryController {

    private final MyProfileQueryService myProfileQueryService;

    // 기본 정보 조회
    @GetMapping("/profile")
    public ApiResponse<MyProfileBasicResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal me) {
        return ApiResponse.success(myProfileQueryService.getMyProfile(me.userId()));
    }

    //기술 스택 조회
    @GetMapping("/tech-stacks")
    public ApiResponse<List<MyTechStackResponse>> getMyTechStacks(@AuthenticationPrincipal UserPrincipal me) {
        return ApiResponse.success(myProfileQueryService.getMyTechStacks(me.userId()));
    }

    // 프로젝트 히스토리 조회
    @GetMapping("/project-history")
    public ApiResponse<List<MyProjectHistoryResponse>> getMyProjectHistory(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(myProfileQueryService.getMyProjectHistory(me.userId(), page, size));
    }
}

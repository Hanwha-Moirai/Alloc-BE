package com.moirai.alloc.profile.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.profile.command.dto.response.MyProfileUpdateResponse;
import com.moirai.alloc.profile.command.dto.response.TechStackDeleteResponse;
import com.moirai.alloc.profile.command.dto.response.TechStackItemResponse;
import com.moirai.alloc.profile.command.dto.request.MyProfileUpdateRequest;
import com.moirai.alloc.profile.command.dto.request.TechStackCreateRequest;
import com.moirai.alloc.profile.command.dto.request.TechStackProficiencyUpdateRequest;
import com.moirai.alloc.profile.command.service.MyProfileCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class MyProfileCommandController {

    private final MyProfileCommandService myProfileCommandService;

    // 기본 정보 수정
    @PutMapping("/profile")
    public ApiResponse<MyProfileUpdateResponse> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal me ,
            @Valid @RequestBody MyProfileUpdateRequest req
    ) {
        Long userId = me.userId();
        return ApiResponse.success(myProfileCommandService.updateMyProfile(userId, req));
    }

    // 기술 스택 등록
    @PostMapping("/tech-stacks")
    public ApiResponse<TechStackItemResponse> createTechStack(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody TechStackCreateRequest req
    ) {
        Long userId = me.userId();
        return ApiResponse.success(myProfileCommandService.createTechStack(userId, req));
    }

    // 기술 스택 숙련도 수정
    @PatchMapping("/tech-stacks/{employeeTechId}/proficiency")
    public ApiResponse<TechStackItemResponse> updateProficiency(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long employeeTechId,
            @Valid @RequestBody TechStackProficiencyUpdateRequest req
    ) {
        Long userId = me.userId();
        return ApiResponse.success(myProfileCommandService.updateProficiency(userId, employeeTechId, req));
    }

    // 기술 스택 삭제
    @DeleteMapping("/tech-stacks/{employeeTechId}")
    public ApiResponse<TechStackDeleteResponse> deleteTechStack(
            @AuthenticationPrincipal UserPrincipal me,
            @PathVariable Long employeeTechId
    ) {
        Long userId = me.userId();
        return ApiResponse.success(myProfileCommandService.deleteTechStack(userId, employeeTechId));
    }


}

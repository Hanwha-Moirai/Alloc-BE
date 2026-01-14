package com.moirai.alloc.meeting.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordSummaryResponse;
import com.moirai.alloc.meeting.query.service.MeetingRecordQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/mydocs/meeting_record")
public class MeetingRecordMyDocsQueryController {

    private final MeetingRecordQueryService meetingRecordQueryService;

    public MeetingRecordMyDocsQueryController(MeetingRecordQueryService meetingRecordQueryService) {
        this.meetingRecordQueryService = meetingRecordQueryService;
    }

    // 내 회의록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> getMyMeetingRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        Page<MeetingRecordSummaryResponse> response =
                meetingRecordQueryService.getMyDocsMeetingRecords(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 내 회의록 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> searchMyMeetingRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        MeetingRecordSearchCondition condition = new MeetingRecordSearchCondition(projectName, from, to);
        Page<MeetingRecordSummaryResponse> response =
                meetingRecordQueryService.searchMyDocsMeetingRecords(principal, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 내 회의록 상세보기
    @GetMapping("/{meetingRecordId}")
    public ResponseEntity<ApiResponse<MeetingRecordDetailResponse>> getMyMeetingRecordDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long meetingRecordId
    ) {
        MeetingRecordDetailResponse response =
                meetingRecordQueryService.getMyDocsMeetingRecordDetail(principal, meetingRecordId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

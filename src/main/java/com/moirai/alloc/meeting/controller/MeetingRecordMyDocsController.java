package com.moirai.alloc.meeting.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.query.dto.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSummaryResponse;
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
public class MeetingRecordMyDocsController {

    private final MeetingRecordQueryService meetingRecordQueryService;

    public MeetingRecordMyDocsController(MeetingRecordQueryService meetingRecordQueryService) {
        this.meetingRecordQueryService = meetingRecordQueryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> getMyMeetingRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable
    ) {
        Page<MeetingRecordSummaryResponse> response =
                meetingRecordQueryService.getMyDocsMeetingRecords(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> searchMyMeetingRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        MeetingRecordSearchCondition condition = new MeetingRecordSearchCondition(projectId, from, to, keyword);
        Page<MeetingRecordSummaryResponse> response =
                meetingRecordQueryService.searchMyDocsMeetingRecords(principal, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

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

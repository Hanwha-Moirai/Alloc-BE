package com.moirai.alloc.meeting.query.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordSummaryResponse;
import com.moirai.alloc.meeting.query.service.MeetingRecordQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/projects/{project_id}/docs/meeting_record")
public class MeetingRecordDocsQueryController {

    private final MeetingRecordQueryService meetingRecordQueryService;

    public MeetingRecordDocsQueryController(MeetingRecordQueryService meetingRecordQueryService) {
        this.meetingRecordQueryService = meetingRecordQueryService;
    }

    // 회의록 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> getMeetingRecords(
            @PathVariable("project_id") Long projectId,
            Pageable pageable
    ) {
        Page<MeetingRecordSummaryResponse> response = meetingRecordQueryService.getDocsMeetingRecords(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회의록 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> searchMeetingRecords(
            @PathVariable("project_id") Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        MeetingRecordSearchCondition condition = new MeetingRecordSearchCondition(projectId, from, to, keyword);
        Page<MeetingRecordSummaryResponse> response =
                meetingRecordQueryService.searchDocsMeetingRecords(projectId, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회의록 상세 조회
    @GetMapping("/{meetingRecordId}")
    public ResponseEntity<ApiResponse<MeetingRecordDetailResponse>> getMeetingRecordDetail(
            @PathVariable("project_id") Long projectId,
            @PathVariable Long meetingRecordId
    ) {
        MeetingRecordDetailResponse response =
                meetingRecordQueryService.getDocsMeetingRecordDetail(projectId, meetingRecordId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

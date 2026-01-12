package com.moirai.alloc.meeting.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.command.dto.request.CreateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.dto.request.UpdateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.service.MeetingRecordCommandService;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
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
@RequestMapping("/api/docs/meeting_record")
public class MeetingRecordDocsController {

    private final MeetingRecordQueryService meetingRecordQueryService;
    private final MeetingRecordCommandService meetingRecordCommandService;

    public MeetingRecordDocsController(MeetingRecordQueryService meetingRecordQueryService,
                                       MeetingRecordCommandService meetingRecordCommandService) {
        this.meetingRecordQueryService = meetingRecordQueryService;
        this.meetingRecordCommandService = meetingRecordCommandService;
    }

    //내 회의록 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> getMeetingRecords(Pageable pageable) {
        Page<MeetingRecordSummaryResponse> response = meetingRecordQueryService.getDocsMeetingRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회의록 목록 조회
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<MeetingRecordSummaryResponse>>> searchMeetingRecords(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        MeetingRecordSearchCondition condition = new MeetingRecordSearchCondition(projectId, from, to, keyword);
        Page<MeetingRecordSummaryResponse> response =
                meetingRecordQueryService.searchDocsMeetingRecords(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회의록 상세 조회
    @GetMapping("/{meetingRecordId}")
    public ResponseEntity<ApiResponse<MeetingRecordDetailResponse>> getMeetingRecordDetail(
            @PathVariable Long meetingRecordId
    ) {
        MeetingRecordDetailResponse response = meetingRecordQueryService.getDocsMeetingRecordDetail(meetingRecordId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회의록 생성
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createMeetingRecord(
            @RequestBody CreateMeetingRecordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long meetingId = meetingRecordCommandService.createMeetingRecord(request, principal);
        return ResponseEntity.ok(ApiResponse.success(meetingId));
    }

    // 회의록 수정
    @PatchMapping("/save")
    public ResponseEntity<ApiResponse<Void>> updateMeetingRecord(
            @RequestBody UpdateMeetingRecordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        meetingRecordCommandService.updateMeetingRecord(request, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회의록 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMeetingRecord(
            @RequestParam Long meetingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        meetingRecordCommandService.deleteMeetingRecord(meetingId, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

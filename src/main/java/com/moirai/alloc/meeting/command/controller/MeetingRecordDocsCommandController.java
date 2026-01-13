package com.moirai.alloc.meeting.command.controller;

import com.moirai.alloc.common.dto.ApiResponse;
import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.meeting.command.dto.request.CreateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.dto.request.UpdateMeetingRecordRequest;
import com.moirai.alloc.meeting.command.service.MeetingRecordCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;

@RestController
@RequestMapping("/api/projects/{project_id}/docs/meeting_record")
public class MeetingRecordDocsCommandController {

    private final MeetingRecordCommandService meetingRecordCommandService;

    public MeetingRecordDocsCommandController(MeetingRecordCommandService meetingRecordCommandService) {
        this.meetingRecordCommandService = meetingRecordCommandService;
    }

    // 회의록 생성
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createMeetingRecord(
            @PathVariable("project_id") Long projectId,
            @RequestBody CreateMeetingRecordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long meetingId = meetingRecordCommandService.createMeetingRecord(projectId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(meetingId));
    }

    // 회의록 저장
    @PatchMapping("/save")
    public ResponseEntity<ApiResponse<Void>> updateMeetingRecord(
            @PathVariable("project_id") Long projectId,
            @RequestBody UpdateMeetingRecordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        meetingRecordCommandService.updateMeetingRecord(projectId, request, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회의록 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMeetingRecord(
            @PathVariable("project_id") Long projectId,
            @RequestParam Long meetingId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        meetingRecordCommandService.deleteMeetingRecord(projectId, meetingId, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

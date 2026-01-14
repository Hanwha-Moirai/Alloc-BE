package com.moirai.alloc.management.api;

import com.moirai.alloc.management.command.service.AcceptAssignment;
import com.moirai.alloc.management.command.service.DecideFinalAssignment;
import com.moirai.alloc.management.command.service.RequestInterview;
import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/assignments/{assignmentId}")
public class AssignmentDecisionController {

    private final AcceptAssignment acceptAssignment;
    private final RequestInterview requestInterview;
    private final DecideFinalAssignment decideFinalAssignment;

    @PostMapping("/response")
    public void respondAssignment(
            @PathVariable Long assignmentId,
            @RequestParam AssignmentStatus status,
            @RequestParam Long userId   // 그냥 식별자; 권한 안 불림
    ) {
        if (status == AssignmentStatus.ACCEPTED) {
            acceptAssignment.acceptAssignment(assignmentId, userId);
        } else if (status == AssignmentStatus.INTERVIEW_REQUESTED) {
            requestInterview.requestInterview(assignmentId, userId);
        } else {
            throw new IllegalArgumentException("Invalid assignment response");
        }
    }

    @PostMapping("/decision")
    public void decideAssignment(
            @PathVariable Long assignmentId,
            @RequestParam FinalDecision decision,
            @RequestParam Long pmUserId   // 그냥 식별자; 권한 안 붙임
    ) {
        decideFinalAssignment.decideFinalAssignment(
                assignmentId,
                pmUserId,
                decision
        );
    }

}

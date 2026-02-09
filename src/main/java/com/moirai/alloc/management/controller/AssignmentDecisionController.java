package com.moirai.alloc.management.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.command.service.AcceptAssignment;
import com.moirai.alloc.management.command.service.DecideFinalAssignment;
import com.moirai.alloc.management.command.service.RequestInterview;
import com.moirai.alloc.management.domain.entity.AssignmentStatus;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    //직원 응답; (사용자만 가능)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/response")
    public void respondAssignment(
            @PathVariable Long projectId,
            @PathVariable Long assignmentId,
            @RequestParam AssignmentStatus status,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.userId();

        if (status == AssignmentStatus.ACCEPTED) {
            acceptAssignment.acceptAssignment(assignmentId, userId);
        } else if (status == AssignmentStatus.INTERVIEW_REQUESTED) {
            requestInterview.requestInterview(assignmentId, userId);
        } else {
            throw new IllegalArgumentException("Invalid assignment response");
        }
    }

    //최종 결정; PM만 가능
    @PreAuthorize("hasRole('PM')")
    @PostMapping("/decision")
    public void decideAssignment(
            @PathVariable Long projectId,
            @PathVariable Long assignmentId,
            @RequestParam FinalDecision decision,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        decideFinalAssignment.decideFinalAssignment(
                assignmentId,
                principal.userId(),
                decision
        );
    }

}

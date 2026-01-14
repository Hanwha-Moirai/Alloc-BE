package com.moirai.alloc.management.api;

import com.moirai.alloc.management.command.controllerdto.AssignmentDecisionRequest;
import com.moirai.alloc.management.command.controllerdto.AssignmentResponseRequest;
import com.moirai.alloc.management.command.controllerdto.AssignmentResponseType;
import com.moirai.alloc.management.command.service.AcceptAssignment;
import com.moirai.alloc.management.command.service.DecideFinalAssignment;
import com.moirai.alloc.management.command.service.RequestInterview;
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

    // 직원 응답 (수락 / 인터뷰 요청)
    @PostMapping("/response")
    public void respondAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentResponseRequest request
    ) {
        if (request.getResponseType() == AssignmentResponseType.ACCEPT) {
            acceptAssignment.acceptAssignment(
                    assignmentId,
                    request.getUserId()
            );
        } else if (request.getResponseType()
                == AssignmentResponseType.INTERVIEW_REQUEST) {
            requestInterview.requestInterview(
                    assignmentId,
                    request.getUserId()
            );
        }
    }

    //PM 최종 결정 (배정 / 제외)
    @PostMapping("/decision")
    public void decideAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentDecisionRequest request
    ) {
        decideFinalAssignment.decideFinalAssignment(
                assignmentId,
                request.getPmUserId(),
                request.getDecision()
        );
    }
}

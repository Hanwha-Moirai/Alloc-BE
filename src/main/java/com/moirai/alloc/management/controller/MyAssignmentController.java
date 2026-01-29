package com.moirai.alloc.management.controller;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.management.query.dto.userAssign.MyPendingAssignmentDTO;
import com.moirai.alloc.management.query.service.GetMyPendingAssignments;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MyAssignmentController {

    private final GetMyPendingAssignments getMyPendingAssignments;

    @GetMapping("/api/me/assignments/pending")
    @PreAuthorize("hasRole('USER')")
    public List<MyPendingAssignmentDTO> getMyPendingAssignments(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return getMyPendingAssignments
                .getMyPendingAssignments(principal.userId());
    }
}

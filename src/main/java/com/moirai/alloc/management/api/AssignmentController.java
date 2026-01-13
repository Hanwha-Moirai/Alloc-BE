package com.moirai.alloc.management.api;

import com.moirai.alloc.management.command.dto.RegisterProjectCommandDTO;
import com.moirai.alloc.management.command.service.RegisterProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final RegisterProject registerProject;

    @PostMapping
    public ResponseEntity<Long> registerProject(
            @RequestBody RegisterProjectCommandDTO command
    ) {
        Long projectId = registerProject.registerProject(command);
        return ResponseEntity.ok(projectId);
    }
}

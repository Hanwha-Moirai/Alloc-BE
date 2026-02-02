package com.moirai.alloc.home.query.service;

import com.moirai.alloc.home.query.dto.HomeProjectSummaryDTO;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.project.command.domain.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetHomeProjectSummary {

    private final SquadAssignmentRepository squadAssignmentRepository;

    public HomeProjectSummaryDTO getSummary(Long userId) {
        List<Project> projects =
                squadAssignmentRepository.findProjectsByUserId(userId);

        LocalDate today = LocalDate.now();

        int active = 0;
        int delayed = 0;
        int closed = 0;

        for (Project p : projects) {
            switch (p.getProjectStatus()) {
                case CLOSED -> closed++;

                case ACTIVE -> {
                    if (p.getEndDate().isBefore(today)) {
                        delayed++;
                    } else {
                        active++;
                    }
                }
            }
        }

        return new HomeProjectSummaryDTO(
                active,
                delayed,
                closed
        );
    }
}

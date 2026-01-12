package com.moirai.alloc.management.domain.policy;

import com.moirai.alloc.project.command.domain.Project;

public interface CandidateSelectionPolicy {
    SelectionResult selectCandidates(Project project);
}

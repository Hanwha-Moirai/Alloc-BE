package com.moirai.alloc.management.domain.policy;

import java.util.List;

public class SelectionResult {
    private final List<Long> candidateUserIds;

    public SelectionResult(List<Long> candidateUserIds) {
        this.candidateUserIds = candidateUserIds;
    }

    public List<Long> getCandidateUserIds() {
        return candidateUserIds;
    }
}

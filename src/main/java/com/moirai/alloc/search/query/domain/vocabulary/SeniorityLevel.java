package com.moirai.alloc.search.query.domain.vocabulary;

public enum SeniorityLevel {
    JUNIOR(1),
    MIDDLE(2),
    SENIOR(3);

    private final int level;

    SeniorityLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }
}

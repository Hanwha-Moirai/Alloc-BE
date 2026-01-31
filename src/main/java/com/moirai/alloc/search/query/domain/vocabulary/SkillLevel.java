package com.moirai.alloc.search.query.domain.vocabulary;

public enum SkillLevel {
    LV1(1), LV2(2), LV3(3);

    private final int level;

    SkillLevel(int level) {
        this.level = level;
    }

    public int number() {
        return level;
    }
}

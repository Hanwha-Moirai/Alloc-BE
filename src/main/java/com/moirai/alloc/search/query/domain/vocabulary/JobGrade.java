package com.moirai.alloc.search.query.domain.vocabulary;

public enum JobGrade {
    INTERN(1),
    STAFF(2),        // 사원
    ASSOCIATE(3),    // 주임
    SENIOR_ASSOCIATE(4), // 대리
    PROJECT_MANAGER(5),      // 과장
    SENIOR_MANAGER(6), // 차장
    DIRECTOR(7),     // 부장
    EXECUTIVE(8)     // 임원

;
    private final int level;
    JobGrade(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }
}

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

    public int getLevel() {
        return level;
    }

    public static JobGrade fromTitleName(String titleName) {
        if (titleName == null) return null;

        return switch (titleName) {
            case "인턴" -> INTERN;
            case "사원" -> STAFF;
            case "주임" -> ASSOCIATE;
            case "대리" -> SENIOR_ASSOCIATE;
            case "과장" -> PROJECT_MANAGER;
            case "차장" -> SENIOR_MANAGER;
            case "부장" -> DIRECTOR;
            case "임원" -> EXECUTIVE;
            default -> null;
        };
    }
}

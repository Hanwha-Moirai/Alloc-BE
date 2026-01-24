package com.moirai.alloc.search.infra.builder;

import java.util.List;
import java.util.stream.Collectors;

public class ExperienceDomainTextBuilder {
    // 프로젝트 '무엇'을 했는지 검색; 도메인 키워드 검색
    private ExperienceDomainTextBuilder() {}

    public static String from(List<String> projectTitles) {
        if (projectTitles == null || projectTitles.isEmpty()) {
            return "";
        }

        return projectTitles.stream()
                .distinct()
                .collect(Collectors.joining(", "));
    }
}

package com.moirai.alloc.search.infra.builder;

import com.moirai.alloc.search.query.domain.model.ProjectType;
import java.util.List;
import java.util.stream.Collectors;

public class ExperienceTextBuilder {

    private ExperienceTextBuilder() {}

    public static String from(List<ProjectType> projectTypes) {
        if (projectTypes == null || projectTypes.isEmpty()) {
            return "프로젝트 경험 없음";
        }

        return projectTypes.stream()
                .distinct()
                .map(ExperienceTextBuilder::toText)
                .collect(Collectors.joining(", "));
    }

    private static String toText(ProjectType type) {
        return switch (type) {
            case NEW -> "신규 프로젝트 경험";
            case OPERATION -> "운영 프로젝트 경험";
            case MAINTENANCE -> "유지보수 프로젝트 경험";
        };
    }
}
package com.moirai.alloc.search.infra.builder;

import java.util.List;
import java.util.stream.Collectors;

public class ExperienceDomainTextBuilder {
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

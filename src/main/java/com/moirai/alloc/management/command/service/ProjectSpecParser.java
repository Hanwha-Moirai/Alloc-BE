package com.moirai.alloc.management.command.service;

import com.moirai.alloc.project.command.domain.Project;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectSpecParser {

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[./-](\\d{1,2})[./-](\\d{1,2})");
    private static final Pattern NUMBER_UNIT_PATTERN = Pattern.compile("(\\d{1,3}(?:,\\d{3})*|\\d+)(\\s*[억만천])?");

    public ProjectSpecMetadata parse(String text) {
        if (text == null) {
            text = "";
        }
        String normalized = text.replace("\r", "");
        Map<String, String> sections = extractSections(normalized);

        String name = findLabelValue(normalized, List.of("프로젝트명", "프로젝트 이름", "프로젝트명칭", "Project Name"));
        LocalDate startDate = null;
        LocalDate endDate = null;

        String periodLine = findLineContaining(normalized, List.of("기간", "일정", "프로젝트 기간"));
        if (periodLine != null) {
            LocalDate[] range = parseDateRange(periodLine);
            if (range != null) {
                startDate = range[0];
                endDate = range[1];
            }
        }

        if (startDate == null) {
            String startValue = findLabelValue(normalized, List.of("시작일", "착수일", "Start Date"));
            startDate = parseDate(startValue);
        }

        if (endDate == null) {
            String endValue = findLabelValue(normalized, List.of("종료일", "완료일", "End Date"));
            endDate = parseDate(endValue);
        }

        String budgetLine = findLabelValue(normalized, List.of("예산", "비용", "금액", "Budget"));
        Integer predictedCost = parseBudget(budgetLine);

        String partners = findLabelValue(normalized, List.of("고객사", "발주처", "파트너", "협력사", "수행사"));

        String description = extractDescription(sections, normalized);

        Project.ProjectType projectType = inferProjectType(normalized);
        Project.ProjectStatus projectStatus = inferProjectStatus(normalized);

        return new ProjectSpecMetadata(
                name,
                startDate,
                endDate,
                predictedCost,
                partners,
                description,
                projectType,
                projectStatus
        );
    }

    private Map<String, String> extractSections(String text) {
        Map<String, StringBuilder> buffer = new LinkedHashMap<>();
        String current = null;
        for (String rawLine : text.split("\n")) {
            String line = rawLine.trim();
            if (line.startsWith("#")) {
                String heading = line.replaceFirst("^#+\\s*", "").trim();
                if (!heading.isEmpty()) {
                    current = heading;
                    buffer.putIfAbsent(current, new StringBuilder());
                }
                continue;
            }
            if (current != null && !line.isEmpty()) {
                buffer.get(current).append(line).append("\n");
            }
        }
        Map<String, String> sections = new LinkedHashMap<>();
        for (Map.Entry<String, StringBuilder> entry : buffer.entrySet()) {
            sections.put(entry.getKey(), entry.getValue().toString().trim());
        }
        return sections;
    }

    private String extractDescription(Map<String, String> sections, String fallbackText) {
        List<String> keys = List.of("프로젝트 개요", "개요", "목적", "배경", "설명", "프로젝트 설명");
        for (String key : keys) {
            for (Map.Entry<String, String> entry : sections.entrySet()) {
                if (entry.getKey().contains(key) && !entry.getValue().isBlank()) {
                    return entry.getValue();
                }
            }
        }
        String[] lines = fallbackText.split("\n");
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (builder.length() > 600) {
                break;
            }
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                builder.append(trimmed).append(" ");
            }
        }
        return builder.toString().trim();
    }

    private String findLabelValue(String text, List<String> labels) {
        for (String label : labels) {
            Pattern pattern = Pattern.compile(label + "\\s*[:：]\\s*(.+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        List<String> lines = Arrays.asList(text.split("\n"));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            for (String label : labels) {
                if (line.equalsIgnoreCase(label) && i + 1 < lines.size()) {
                    return lines.get(i + 1).trim();
                }
            }
        }
        return null;
    }

    private String findLineContaining(String text, List<String> keywords) {
        for (String line : text.split("\n")) {
            for (String keyword : keywords) {
                if (line.contains(keyword)) {
                    return line.trim();
                }
            }
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace("년", "-")
                .replace("월", "-")
                .replace("일", "")
                .replace("/", "-")
                .replace(".", "-")
                .trim();
        Matcher matcher = DATE_PATTERN.matcher(normalized);
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                return LocalDate.of(year, month, day);
            } catch (DateTimeParseException | IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDate[] parseDateRange(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = DATE_PATTERN.matcher(value.replace("/", "-").replace(".", "-"));
        List<LocalDate> dates = new ArrayList<>();
        while (matcher.find()) {
            LocalDate date = parseDate(matcher.group(0));
            if (date != null) {
                dates.add(date);
            }
        }
        if (dates.size() >= 2) {
            return new LocalDate[]{dates.get(0), dates.get(1)};
        }
        return null;
    }

    private Integer parseBudget(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        long total = 0L;
        Matcher matcher = NUMBER_UNIT_PATTERN.matcher(value.replace(" ", ""));
        while (matcher.find()) {
            String numberRaw = matcher.group(1).replace(",", "");
            String unit = matcher.group(2) == null ? "" : matcher.group(2).trim();
            long number = Long.parseLong(numberRaw);
            switch (unit) {
                case "억":
                    total += number * 100_000_000L;
                    break;
                case "만":
                    total += number * 10_000L;
                    break;
                case "천":
                    total += number * 1_000L;
                    break;
                default:
                    total += number;
            }
        }
        if (total == 0) {
            return null;
        }
        return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    private Project.ProjectType inferProjectType(String text) {
        if (text.contains("유지보수") || text.contains("유지 보수")) {
            return Project.ProjectType.MAINTENANCE;
        }
        if (text.contains("운영")) {
            return Project.ProjectType.OPERATION;
        }
        if (text.contains("신규") || text.contains("개발")) {
            return Project.ProjectType.NEW;
        }
        return null;
    }

    private Project.ProjectStatus inferProjectStatus(String text) {
        if (text.contains("진행중") || text.contains("진행")) {
            return Project.ProjectStatus.ACTIVE;
        }
        if (text.contains("종료")) {
            return Project.ProjectStatus.CLOSED;
        }
        if (text.contains("보류") || text.contains("중단")) {
            return Project.ProjectStatus.HOLD;
        }
        if (text.contains("초안") || text.contains("DRAFT")) {
            return Project.ProjectStatus.DRAFT;
        }
        return null;
    }
}

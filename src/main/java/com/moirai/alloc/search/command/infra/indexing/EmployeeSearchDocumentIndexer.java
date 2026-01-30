package com.moirai.alloc.search.command.infra.indexing;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.profile.command.repository.EmployeeSkillRepository;
import com.moirai.alloc.search.command.infra.builder.ProfileSummaryBuilder;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.common.xcontent.XContentType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class EmployeeSearchDocumentIndexer {

    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final SquadAssignmentRepository squadAssignmentRepository;
    private final RestHighLevelClient client;

    @Transactional(readOnly = true)
    public void reindex(Long employeeId) {

        Employee employee = employeeRepository.findByIdForIndexing(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        // 기술 숙련도
        List<TechSkillRow> techSkillRows =
                employeeSkillRepository.findTechSkillsForIndexing(employeeId);

        Map<String, SkillLevel> techSkills =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                TechSkillRow::getTechName,
                                row -> SkillLevel.valueOf(
                                        row.getProficiency().name()
                                )
                        ));


        Map<String, Integer> techSkillLevels =
                techSkills.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().number()
                        ));

        int activeProjectCount =
                squadAssignmentRepository.countActiveProjects(employeeId);

        SeniorityLevel seniorityLevel = resolveSeniority(employee);
        JobGrade jobGrade =
                JobGrade.fromTitleName(employee.getTitleStandard().getTitleName());

        PersonDocument document = PersonDocument.builder()
                .personId(employee.getUserId())
                .name(employee.getUser().getUserName())
                .jobTitle(employee.getTitleStandard().getTitleName())
                .department(employee.getDepartment().getDeptName())
                .seniorityLevelLevel(seniorityLevel.level())
                .jobGradeLevel(jobGrade.getLevel())
                .techSkills(techSkills)
                .techSkillNumericLevels(techSkillLevels)
                .activeProjectCount(activeProjectCount)
                .profileSummary(
                        ProfileSummaryBuilder.build(employee, techSkills)
                )
                .experienceDomainText(
                        buildExperienceText(employee.getUserId())
                )
                .build();

        save(document);
    }
    private String buildExperienceText(Long employeeId) {
        List<String> titles =
                squadAssignmentRepository.findExperiencedProjectTitles(employeeId);

        return String.join(" ", titles);
    }

    private void save(PersonDocument document) {
        try {
            IndexRequest request = new IndexRequest("people_index")
                    .id(document.getPersonId().toString())
                    .source(
                            Map.ofEntries(
                                    Map.entry("personId", document.getPersonId()),
                                    Map.entry("name", document.getName()),
                                    Map.entry("jobTitle", document.getJobTitle()),
                                    Map.entry("department", document.getDepartment()),
                                    Map.entry("seniorityLevelLevel", document.getSeniorityLevelLevel()),
                                    Map.entry("jobGradeLevel", document.getJobGradeLevel()),
                                    Map.entry("techSkills", document.getTechSkills()),
                                    Map.entry("techSkillNumericLevels", document.getTechSkillNumericLevels()),
                                    Map.entry("activeProjectCount", document.getActiveProjectCount()),
                                    Map.entry("profileSummary", document.getProfileSummary()),
                                    Map.entry("experienceDomainText", document.getExperienceDomainText())
                            ),
                            XContentType.JSON
                    );

            client.index(request, RequestOptions.DEFAULT);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to index employee", e);
        }
    }

    private SeniorityLevel resolveSeniority(Employee employee) {
        if (employee == null || employee.getTitleStandard() == null) {
            return SeniorityLevel.JUNIOR;
        }

        JobGrade grade =
                JobGrade.fromTitleName(employee.getTitleStandard().getTitleName());

        if (grade == null) {
            return SeniorityLevel.JUNIOR;
        }

        if (grade.getLevel() >= JobGrade.SENIOR_MANAGER.getLevel()) {
            return SeniorityLevel.SENIOR;
        }

        if (grade.getLevel() >= JobGrade.SENIOR_ASSOCIATE.getLevel()) {
            return SeniorityLevel.MIDDLE;
        }

        return SeniorityLevel.JUNIOR;
    }
    @Transactional(readOnly = true)
    public void reindexAll() {
        List<Long> employeeIds = employeeRepository.findAllIdsForIndexing();
        for (Long employeeId : employeeIds) {
            reindex(employeeId);
        }
    }

}

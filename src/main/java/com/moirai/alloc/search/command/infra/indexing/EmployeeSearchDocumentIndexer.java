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

import java.util.HashMap;
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

        System.out.println("▶ reindex employeeId = " + employeeId);

        Employee employee = employeeRepository.findByIdForIndexing(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        System.out.println("▶ employee found = " + employee.getUser().getUserName());
        // 기술 숙련도
        List<TechSkillRow> techSkillRows =
                employeeSkillRepository.findTechSkillsForIndexing(employeeId);
        System.out.println("▶ techSkillRows size = " + techSkillRows.size());
        techSkillRows.forEach(row ->
                System.out.println("▶ skill = " + row.getTechName()
                        + " / " + row.getProficiency())
        );
        Map<String, SkillLevel> techSkills =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                TechSkillRow::getTechName,
                                row -> SkillLevel.valueOf(row.getProficiency().name())
                        ));


        Map<String, Integer> techSkillLevels =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                TechSkillRow::getTechName,
                                row -> row.getProficiency().ordinal() + 1
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
            System.out.println("▶ indexing personId = " + document.getPersonId());

            Map<String, Object> source = new HashMap<>();

            source.put("personId", document.getPersonId());
            source.put("name", document.getName());
            source.put("jobTitle", document.getJobTitle());
            source.put("department", document.getDepartment());
            source.put("seniorityLevelLevel", document.getSeniorityLevelLevel());
            source.put("jobGradeLevel", document.getJobGradeLevel());
            source.put("activeProjectCount", document.getActiveProjectCount());
            source.put("profileSummary", document.getProfileSummary());
            source.put("experienceDomainText", document.getExperienceDomainText());

            if (!document.getTechSkills().isEmpty()) {
                source.put("techSkills", document.getTechSkills());
                source.put("techSkillNumericLevels", document.getTechSkillNumericLevels());
            }

            IndexRequest request = new IndexRequest("people_index")
                    .id(document.getPersonId().toString())
                    .source(source);

            client.index(request, RequestOptions.DEFAULT);

            System.out.println("✅ indexed personId = " + document.getPersonId());

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Failed to index employee: " + e.getMessage(),
                    e
            );
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

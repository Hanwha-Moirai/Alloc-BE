package com.moirai.alloc.search.command.infra.indexing;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.profile.command.repository.EmployeeSkillRepository;
import com.moirai.alloc.search.command.infra.builder.ProfileEmbeddingTextBuilder;
import com.moirai.alloc.search.command.infra.builder.ProfileSummaryBuilder;
import com.moirai.alloc.search.command.infra.embedding.EmbeddingGenerator;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
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
    private final EmbeddingGenerator embeddingGenerator;

    @Transactional(readOnly = true)
    public void reindex(Long employeeId) {

        Employee employee = employeeRepository.findByIdForIndexing(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        // 기술 숙련도
        List<TechSkillRow> techSkillRows =
                employeeSkillRepository.findTechSkillsForIndexing(employeeId);

        techSkillRows.forEach(row ->
                System.out.println("▶ skill = " + row.getTechName()
                        + " / " + row.getProficiency())
        );
        Map<String, SkillLevel> techSkills =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                row -> row.getTechName().toUpperCase().replace(" ", "_"),
                                row -> SkillLevel.valueOf(row.getProficiency().name())
                        ));


        Map<String, Integer> techSkillLevels =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                row -> row.getTechName().toUpperCase().replace(" ", "_"),
                                row -> row.getProficiency().ordinal() + 1
                        ));

        int activeProjectCount =
                squadAssignmentRepository.countActiveProjects(employeeId);

        SeniorityLevel seniorityLevel = resolveSeniority(employee);
        JobGrade jobGrade =
                JobGrade.fromTitleName(employee.getTitleStandard().getTitleName());
        JobRole jobRole = resolveJobRole(employee);


        String embeddingText =
                ProfileEmbeddingTextBuilder.build(
                        employee,
                        techSkills,
                        buildExperienceText(employee.getUserId())
                );

        float[] embedding =
                embeddingGenerator.generate(embeddingText);

        PersonDocument document = PersonDocument.builder()
                .personId(employee.getUserId())
                .name(employee.getUser().getUserName())
                .jobTitle(employee.getTitleStandard().getTitleName())
                .department(employee.getDepartment().getDeptName())
                .jobRole(jobRole.name())
                .seniorityLevelLevel(seniorityLevel.level())
                .jobGradeLevel(jobGrade.getLevel())
                .techSkills(techSkills)
                .techSkillNumericLevels(techSkillLevels)
                .activeProjectCount(activeProjectCount)
                .profileSummary(ProfileSummaryBuilder.build(employee, techSkills))
                .experienceDomainText(buildExperienceText(employee.getUserId()))
                .profileEmbedding(embedding)
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

            Map<String, Object> source = new HashMap<>();

            source.put("personId", document.getPersonId());
            source.put("name", document.getName());
            source.put("jobTitle", document.getJobTitle());
            source.put("department", document.getDepartment());
            source.put("jobRole", document.getJobRole());
            source.put("seniorityLevelLevel", document.getSeniorityLevelLevel());
            source.put("jobGradeLevel", document.getJobGradeLevel());
            source.put("activeProjectCount", document.getActiveProjectCount());
            source.put("profileSummary", document.getProfileSummary());
            source.put("experienceDomainText", document.getExperienceDomainText());
            source.put("profileEmbedding", document.getProfileEmbedding());

            if (!document.getTechSkills().isEmpty()) {
                source.put("techSkills", document.getTechSkills());
                source.put("techSkillNumericLevels", document.getTechSkillNumericLevels());
            }

            IndexRequest request = new IndexRequest("people_index")
                    .id(document.getPersonId().toString())
                    .source(source);

            client.index(request, RequestOptions.DEFAULT);


        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    "Failed to index employee: " + e.getMessage(),
                    e
            );
        }
    }
    private JobRole resolveJobRole(Employee employee) {

        //  직무명 기반
        if (employee.getJob() != null) {
            String jobName =
                    employee.getJob().getJobName().toLowerCase();

            if (jobName.contains("백엔드") || jobName.contains("서버")) {
                return JobRole.BACKEND;
            }
            if (jobName.contains("프론트")) {
                return JobRole.FRONTEND;
            }
            if (jobName.contains("인프라") || jobName.contains("devops")) {
                return JobRole.INFRA;
            }
            if (jobName.contains("데이터")) {
                return JobRole.DATA;
            }
        }

        //  기술 스택 기반 fallback
        List<String> techs =
                employeeSkillRepository
                        .findTechSkillsForIndexing(employee.getUserId())
                        .stream()
                        .map(r -> r.getTechName().toLowerCase())
                        .toList();

        if (techs.contains("java") || techs.contains("spring")) {
            return JobRole.BACKEND;
        }
        if (techs.contains("react") || techs.contains("vue")) {
            return JobRole.FRONTEND;
        }
        if (techs.contains("docker") || techs.contains("kubernetes")) {
            return JobRole.INFRA;
        }

        return JobRole.ETC;
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

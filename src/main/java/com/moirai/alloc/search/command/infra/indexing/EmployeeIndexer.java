package com.moirai.alloc.search.command.infra.indexing;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.profile.command.repository.EmployeeSkillRepository;
import com.moirai.alloc.search.command.infra.builder.ProfileSummaryBuilder;
import com.moirai.alloc.search.command.infra.builder.SeniorityLevelBuilder;
import com.moirai.alloc.search.command.infra.opensearch.OpenSearchPersonWriter;
import com.moirai.alloc.search.query.domain.vocabulary.JobGrade;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EmployeeIndexer {
    // 조회 + 조립만 한다.
    // 검색 인덱스에 필요한 직원 데이터를 db에서 효율적으로 수집하고,
    // 이를 persondocument로 조립하는 인프라 컴포넌트
    // 검색용 데이터를 조립하는 역할
    // 직원 1명에 대한 검색에 필요한 데이터 조회.
    // 연관 테이블, fetch, batch 전략 결정
    // PersonDocument.from 호출
    // writer에게 위임
    private final EmployeeRepository employeeRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final SquadAssignmentRepository squadAssignmentRepository;
    private final OpenSearchPersonWriter writer;

    @Transactional(readOnly = true)
    public void reindex(Long employeeId) {
        // 검색 인덱싱(fetch join)
        Employee employee = employeeRepository.findByIdForIndexing(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        // 숙련도
        List<TechSkillRow> techSkillRows = employeeSkillRepository.findTechSkillsForIndexing(employeeId);
        // 기술 상태 의미적 표현 (정확 일치 검색)
        Map<String, SkillLevel> techSkills =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                TechSkillRow::techName,
                                row -> SkillLevel.valueOf(row.proficiency().name())
                        ));
        // 기술 상태 범위 표현 (범위용)
        Map<String, Integer> techSkillLevels =
                techSkills.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().number()
                        ));

        // 현재 투입 중인 프로젝트 수
        int activeProjectCount = squadAssignmentRepository.countActiveProjects(employeeId);

        SeniorityLevel seniorityLevel = SeniorityLevelBuilder.from(employee);

        JobGrade jobGrade = JobGrade.fromTitleName(employee.getTitleStandard().getTitleName());

        // PersonDocument 조립
        PersonDocument document = PersonDocument.builder()
                .personId(employee.getUserId())
                .name(employee.getUser().getUserName())
                .jobTitle(employee.getTitleStandard().getTitleName())
                .department(employee.getDepartment().getDeptName())
                // enum / range 검색용 필드들 채우기
                .seniorityLevelLevel(seniorityLevel.level())
                .jobGradeLevel(jobGrade.getLevel())
                .techSkills(techSkills)
                .techSkillNumericLevels(techSkillLevels)
                .activeProjectCount(activeProjectCount)
                .profileSummary(
                        ProfileSummaryBuilder.build(employee, techSkills)
                )
                .build();

        // OpenSearch 저장
        writer.save(document);
    }

    @Transactional(readOnly = true)
    public void reindexAll() {
        List<Long> employeeIds = employeeRepository.findAllIdsForIndexing();
        for (Long employeeId : employeeIds) {
            reindex(employeeId);
        }
    }
}

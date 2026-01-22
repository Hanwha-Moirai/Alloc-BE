package com.moirai.alloc.search.infra.indexing;

import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.profile.command.repository.EmployeeSkillRepository;
import com.moirai.alloc.project.command.domain.Project;
import com.moirai.alloc.search.infra.builder.ExperienceTextBuilder;
import com.moirai.alloc.search.infra.builder.ProfileSummaryBuilder;
import com.moirai.alloc.search.infra.opensearch.OpenSearchPersonWriter;
import com.moirai.alloc.search.query.domain.model.ProjectType;
import com.moirai.alloc.search.query.domain.model.SkillLevel;
import com.moirai.alloc.search.query.domain.model.WorkingType;
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
        List<TechSkillRow> techSkillRows =
                employeeSkillRepository.findTechSkillsForIndexing(employeeId);

        Map<String, SkillLevel> techSkills =
                techSkillRows.stream()
                        .collect(Collectors.toMap(
                                TechSkillRow::techName,
                                row -> SkillLevel.valueOf(row.proficiency().name())
                        ));

        // 현재 투입 중인 프로젝트 수
        int activeProjectCount =
                squadAssignmentRepository.countActiveProjects(employeeId);

        // 경험 텍스트용 프로젝트 타입
        List<Project.ProjectType> experiencedProjectTypes =
                squadAssignmentRepository.findExperiencedProjectTypes(employeeId);

        List<ProjectType> searchProjectTypes =
                experiencedProjectTypes.stream()
                        .map(pt -> ProjectType.valueOf(pt.name()))
                        .toList();

        String experience =
                ExperienceTextBuilder.from(searchProjectTypes);

        //검색용 Document 조립 (Projection)
        PersonDocument document = PersonDocument.builder()
                .personId(employee.getUserId())
                .name(employee.getUser().getUserName())
                .jobTitle(employee.getTitleStandard().getTitleName())
                .department(employee.getDepartment().getDeptName())
                .workingType(WorkingType.valueOf(employee.getEmployeeType().name()))
                .techSkills(techSkills)
                .activeProjectCount(activeProjectCount)
                .experience(experience)
                .profileSummary(ProfileSummaryBuilder.build(
                        employee, techSkills, experience
                ))
                .build();


        // OpenSearch 저장
        writer.save(document);
    }

    //전체 직원 인덱싱- 초기 인덱스 생성- 운영 중 재빌드

    @Transactional(readOnly = true)
    public void reindexAll() {

        List<Long> employeeIds =
                employeeRepository.findAllIdsForIndexing();

        for (Long employeeId : employeeIds) {
            reindex(employeeId);
        }
    }
}

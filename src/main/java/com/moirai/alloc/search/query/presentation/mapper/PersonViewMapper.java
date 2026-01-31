package com.moirai.alloc.search.query.presentation.mapper;

import com.moirai.alloc.search.query.domain.vocabulary.SkillLevel;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.presentation.dto.PersonView;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersonViewMapper {
    //PersonDocument 를 PersonView로 변환. 어떤 필드에 담을 지 결정(변환)
    public PersonView toView(PersonDocument doc) {

        SkillLevel representativeSkill =
                doc.getTechSkills() == null
                        ? null
                        : doc.getTechSkills().values().stream()
                        .max(Enum::compareTo)
                        .orElse(null);

        return PersonView.builder()
                .personId(doc.getPersonId())
                .name(doc.getName())
                .jobTitle(doc.getJobTitle())
                .jobRole(doc.getJobRole())
                //기술요약
                .techNames(
                        doc.getTechSkills() == null
                                ? List.of()
                                : new ArrayList<>(doc.getTechSkills().keySet())
                )
                .representativeSkillLevel(representativeSkill)
                //상태정보
                .activeProjectCount(doc.getActiveProjectCount())
                .department(doc.getDepartment())
                .profileSummary(doc.getProfileSummary())
                .build();
    }
    public List<PersonView> toViews(List<PersonDocument> docs) {
        return docs.stream()
                .map(this::toView)
                .toList(); // Java 16+ (아니면 collect)
    }
}

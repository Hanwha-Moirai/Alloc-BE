package com.moirai.alloc.search.query.presentation.mapper;

import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.presentation.dto.PersonView;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonViewMapper {
    //PersonDocument 를 PersonView로 변환. 어떤 필드에 담을 지 결정(변환)
    public PersonView toView(PersonDocument doc) {
        return PersonView.builder()
                .personId(doc.getPersonId())
                .name(doc.getName())
                .jobTitle(doc.getJobTitle())
                .techs(doc.getTechs())
                .skillLevel(doc.getSkillLevel())
                .activeProjectCount(doc.getActiveProjectCount())
                .department(doc.getDepartment())
                .workingType(doc.getWorkingType())
                .build();
    }

    public List<PersonView> toViews(List<PersonDocument> docs) {
        return docs.stream()
                .map(this::toView)
                .toList(); // Java 16+ (아니면 collect)
    }
}

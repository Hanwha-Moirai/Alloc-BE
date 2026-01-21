package com.moirai.alloc.search.query.api;

import com.moirai.alloc.search.query.infra.openSearch.OpenSearchPersonSearcher;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.model.PersonView;
import com.moirai.alloc.search.query.model.SearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/people")
public class PeopleSearchController {
    private final OpenSearchPersonSearcher searcher;

    @PostMapping("/search")
    public List<PersonView> search(@RequestBody SearchCondition condition) {
        List<PersonDocument> documents = searcher.search(condition);
        return documents.stream()
                .map(this::toView)
                .toList();
    }
    private PersonView toView(PersonDocument doc) {
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
}

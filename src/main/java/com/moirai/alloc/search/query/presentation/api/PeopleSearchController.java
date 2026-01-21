package com.moirai.alloc.search.query.presentation.api;

import com.moirai.alloc.search.query.infra.openSearch.OpenSearchPersonSearcher;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import com.moirai.alloc.search.query.presentation.dto.PersonView;
import com.moirai.alloc.search.query.presentation.mapper.PersonViewMapper;
import com.moirai.alloc.search.query.domain.model.SearchCondition;
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
    private final PersonViewMapper mapper;

    @PostMapping("/search")
    public List<PersonView> search(@RequestBody SearchCondition condition) {
        List<PersonDocument> documents = searcher.search(condition);

        return mapper.toViews(documents);
    }

}

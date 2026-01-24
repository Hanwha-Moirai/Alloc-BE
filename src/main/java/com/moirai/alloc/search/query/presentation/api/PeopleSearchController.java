package com.moirai.alloc.search.query.presentation.api;

import com.moirai.alloc.search.query.presentation.dto.PersonView;
import com.moirai.alloc.search.query.service.SearchPeopleByNaturalLanguage;
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
    private final SearchPeopleByNaturalLanguage searchPeopleByNaturalLanguage;

    @PostMapping("/search")
    public List<PersonView> search(@RequestBody String query) {
        return searchPeopleByNaturalLanguage.search(query);
    }
}

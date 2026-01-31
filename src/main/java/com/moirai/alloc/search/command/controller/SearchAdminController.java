package com.moirai.alloc.search.command.controller;

import com.moirai.alloc.search.command.service.IndexPeopleToSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchAdminController {

    private final IndexPeopleToSearch indexPeopleToSearch;

    @PostMapping("/reindex")
    public void reindexAll() {
        indexPeopleToSearch.reindexAll();
    }

}

package com.moirai.alloc.search.command.infra.opensearch;

import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenSearchPersonWriter {
    // 저장만 담당
    private final RestHighLevelClient client;

    public void save(PersonDocument doc){

    }
}

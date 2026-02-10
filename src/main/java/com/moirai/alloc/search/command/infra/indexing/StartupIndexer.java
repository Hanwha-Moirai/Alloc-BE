package com.moirai.alloc.search.command.infra.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupIndexer {

    private final EmployeeSearchDocumentIndexer indexer;

    @EventListener(ApplicationReadyEvent.class)
    public void reindexOnStartup(){
        indexer.reindexAll();
    }
}

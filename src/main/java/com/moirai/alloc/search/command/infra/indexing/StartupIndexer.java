package com.moirai.alloc.search.command.infra.indexing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupIndexer {

    private final EmployeeSearchDocumentIndexer indexer;

    @EventListener(ApplicationReadyEvent.class)
    public void reindexOnStartup(){
        try {
            indexer.reindexAll();
        } catch (Exception e) {
            log.error("Startup reindex failed (ignore)", e);
        }
    }
}

package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;

public interface SearchIntentParser {
    SearchIntent parse(String naturalLanguage);
}

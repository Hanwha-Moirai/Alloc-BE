package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.model.SearchIntent;

public interface SearchIntentParser {
    SearchIntent parse(String naturalLanguage);
}

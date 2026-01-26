package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;

public class ConversationContext {
    // 대화 기억의 최소 단위
    // 이전 검색 조건 저장, 다음 검색의 기준 점 제공
    private SearchIntent lastIntent;
    public SearchIntent getLastIntent() {
        return lastIntent;
    }

    public void save(SearchIntent intent) {
        this.lastIntent = intent;
    }

    public void reset() {
        this.lastIntent = null;
    }
}

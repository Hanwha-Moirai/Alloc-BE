package com.moirai.alloc.search.query.infra.gpt;

import com.moirai.alloc.search.query.domain.intent.SearchIntent;


public interface GptClient {
    // gpt를 파서로 취급하기 위한 인터페이스
    SearchIntent parseIntent(String prompt);
    //TODO ; GPT 연동 구현 필요
}

package com.moirai.alloc.search.query.infra.gpt.client;

public interface GptClient {
    // 순수 텍스트 생성 클라이어튼로 단순화; 프롬프트 -> 문자열로
    //TODO ; GPT 연동 구현 필요
    String ask(String prompt);
}

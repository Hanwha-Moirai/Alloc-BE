package com.moirai.alloc.search.query.domain.vocabulary;
// 검색은 도메인 상태가 아니라 검색 언어(QueryModel)을 사용한다.
// 검색컨텍스트 분리를 위해 Search 전용 ProjectType enum 추가
public enum ProjectType {
    NEW, OPERATION, MAINTENANCE
}

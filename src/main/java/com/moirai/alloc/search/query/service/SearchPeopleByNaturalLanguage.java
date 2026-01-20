package com.moirai.alloc.search.query.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchPeopleByNaturalLanguage {
    // 사람을 자연어로 검색한다.
    //1. 자연어 -> 구조화
    //2. 검색 조건 생성
    //3. openSearch 조회
    //4. view 반환

}

package com.moirai.alloc.search.command.service;

import com.moirai.alloc.search.command.infra.indexing.EmployeeSearchDocumentIndexer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexPeopleToSearch {
    // DB에서 직원 데이터 조회(직원을 검색 인덱스에 반영)
    // PERSONDOCUMENT로 변환
    // OPENSEARCH에 저장시키기

    private final EmployeeSearchDocumentIndexer employeeIndexer;

    public void indexEmployee(Long employeeId) {
        employeeIndexer.reindex(employeeId);
    }

    // 전체 직원 검색 인덱스 재생성용도, 초기 배포, 인덱스 손상 복구, 운영자 수동 실행 용도
    public void reindexAll() {
        employeeIndexer.reindexAll();
    }

}

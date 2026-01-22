package com.moirai.alloc.search.command.service;

import com.moirai.alloc.search.infra.indexing.EmployeeIndexer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexPeopleToSearch {
    // DB에서 직원 데이터 조회(직원을 검색 인덱스에 반영)
    // PERSONDOCUMENT로 변환
    // OPENSEARCH에 저장시키기

    private final EmployeeIndexer employeeIndexer;

    public void indexEmployee(Long employeeId) {
        employeeIndexer.reindex(employeeId);
    }
    public void reindexAll() {
        employeeIndexer.reindexAll();
    }

}

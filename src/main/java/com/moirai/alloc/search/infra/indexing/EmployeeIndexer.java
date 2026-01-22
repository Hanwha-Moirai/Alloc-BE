package com.moirai.alloc.search.infra.indexing;

import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.search.infra.opensearch.OpenSearchPersonWriter;
import com.moirai.alloc.search.query.infra.openSearch.PersonDocument;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeIndexer {
    // 검색용 데이터를 조립하는 역할
    // 검색 인덱스에 필요한 데이터를 DB에서 효율적으로 모아서 검색 전용 문서(PersonDocumne)로 변환
    //fetch join, / batch join 전략 사용. 어떤 필드를 검색 인덱스에 넣을 지 결정
    private final EmployeeRepository employeeRepository;
    private final OpenSearchPersonWriter writer;

    @Transactional(readOnly = true)
    public void reindex(Long employeeId) {
        Employee employee = employeeRepository.findeByIdForIndexing(employeeId);

        PersonDocument doc = PersonDocument.from(employee);
        writer.save(doc);
    }
}

package com.moirai.alloc.management.query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class GetProjectDetail {
    public void getProjectDetail(Long projectId) {
//        1) projectId로 프로젝트를 식별한다
//        2) 프로젝트 상세 정보를 조회한다.
//        3) 조회 결과를 반환한다

    }
}

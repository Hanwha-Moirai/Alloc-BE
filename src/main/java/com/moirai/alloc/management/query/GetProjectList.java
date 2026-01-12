package com.moirai.alloc.management.query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class GetProjectList {
    public void getProjectList(Long userId){
//        1) userId를 기준으로 사용자 식별
//        2) 해당 사용자가 참여하고 참여했던(진행/종료) 프로젝트 목록을 조회한다.
//        3) 프로젝트 목록을 조회용 형태로 반환한다.
    }
}

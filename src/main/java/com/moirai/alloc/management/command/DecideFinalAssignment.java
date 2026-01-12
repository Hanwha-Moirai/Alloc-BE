package com.moirai.alloc.management.command;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DecideFinalAssignment {
    public void decideFinalAssignment(Long assignmentId, FinalDecision decision){
//        1)  assignmentId로 배정을 찾는다.
//        2) 요청자가 pm 권한인지 확인한다(행위 주체 검증)
//        3) 배정에 대하여 최종 decision을 내리라고 한다.
    }

}

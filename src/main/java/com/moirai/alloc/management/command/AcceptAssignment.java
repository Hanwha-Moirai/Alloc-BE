package com.moirai.alloc.management.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AcceptAssignment {

    public void acceptAssignment(Long assignmentId, Long userId){
//        1) assignmentId로 배정을 찾는다.
//        2) 이 배정 대상이 userId가 맞는지 확인한다(행위 주체 검증)
//        3) 배정된 유저에게 수락하라고 말한다.

    }
}

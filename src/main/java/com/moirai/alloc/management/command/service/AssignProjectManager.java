package com.moirai.alloc.management.command.service;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignProjectManager {
//    RegisterProject에서 호출되는 PM 자동 배정 정책 실행 서비스
//    SquadAssignment를 생성하여 프로젝트와 PM 간의 참여 관계를 확정한다.
    private final SquadAssignmentRepository squadAssignmentRepository;

    public AssignProjectManager(SquadAssignmentRepository squadAssignmentRepository) {
        this.squadAssignmentRepository = squadAssignmentRepository;
    }

    public void assignPm(Long projectId, Long pmUserId) {

        // PM 배정 표현하는 SquadAssignment - pm 자동 배정용 팩토리 메서드 호출 및 생성
        SquadAssignment assignment =
                SquadAssignment.assignPm(projectId, pmUserId);
        //생성된 assignment를 저장하여 project와 pm간의  참여 관계 영속화
        squadAssignmentRepository.save(assignment);
    }
}

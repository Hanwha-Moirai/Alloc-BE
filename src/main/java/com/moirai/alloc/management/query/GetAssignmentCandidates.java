package com.moirai.alloc.management.query;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class GetAssignmentCandidates {
    private final SquadAssignmentRepository assignmentRepository;

    public GetAssignmentCandidates(
            SquadAssignmentRepository assignmentRepository
    ) {
        this.assignmentRepository = assignmentRepository;
    }
    public void getAssignmentCandidates(Long projectId, Long userId){
//        1) userId가 PM 권한인지 확인한다, (행위주체 확인)
//        2) projectId로 프로젝트를 식별한다
//        3) 해당 프로젝트에 대해 생성된 배정 후보 목록을 조회한다.
//        4) 후보 목록을 조회용 형태로 반환한다.

    }
}

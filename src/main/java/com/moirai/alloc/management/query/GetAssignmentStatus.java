package com.moirai.alloc.management.query;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class GetAssignmentStatus {
    private final SquadAssignmentRepository assignmentRepository;
    //private final ProjectRepository projectRepository;
    public GetAssignmentStatus(
            SquadAssignmentRepository assignmentRepository
    ) {
        this.assignmentRepository = assignmentRepository;
    }
    public void getAssignmentFinalStatus(Long assignmentId){
//        1) assignmentId로 배정을 식별한다.
//        2) 해당 배정의 최종 상태 정보 목록을 조회한다.
//        4) 최종 상태 정보 목록을 조회용 형태로 반환한다.

    }
}

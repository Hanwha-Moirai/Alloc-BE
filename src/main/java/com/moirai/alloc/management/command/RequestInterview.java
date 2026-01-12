package com.moirai.alloc.management.command;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RequestInterview {
    private final SquadAssignmentRepository assignmentRepository;

    public RequestInterview(SquadAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }
    public void requestInterview(Long assignmentId, Long userId){
//        1) assignmentId로 배정을 찾는다.
//        2) 이 배정된 대상이 userId가 맞는지 확인한다.(행위 주체 검증)
//        3) 배정된 유저에게 인터뷰 요청하라고 말한다.
        SquadAssignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        if(!assignment.getUserId().equals(userId)){
            throw new IllegalArgumentException("Only the assigned user can request Interview for this assignment");
        }
        assignment.requestInterview();
    }
}

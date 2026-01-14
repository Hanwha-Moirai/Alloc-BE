package com.moirai.alloc.management.command.service;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DecideFinalAssignment {
    private final SquadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    public DecideFinalAssignment(SquadAssignmentRepository assignmentRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }
    public void decideFinalAssignment(Long assignmentId, Long pmUserId, FinalDecision decision){
//        1)  assignmentId로 배정을 찾는다.
//        2) 요청자가 pm 권한인지 확인한다(행위 주체 검증)
//        3) 배정에 대하여 최종 decision을 내리라고 한다.
        SquadAssignment assignment = assignmentRepository.findById(assignmentId).orElseThrow(() -> new RuntimeException("Assignment not found"));
        User pm =
                userRepository.findById(pmUserId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (pm.getAuth() != User.Auth.PM) {
            throw new IllegalStateException("PM만 최종 결정을 할 수 있습니다.");
        }

        if (decision == FinalDecision.ASSIGNED) {
            assignment.finalAssign();
        } else if (decision == FinalDecision.EXCLUDED) {
            assignment.finalExclude();
        }
    }

}

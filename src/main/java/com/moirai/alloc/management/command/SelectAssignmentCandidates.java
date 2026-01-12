package com.moirai.alloc.management.command;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SelectAssignmentCandidates {
    private final SquadAssignmentRepository assignmentRepository;
    //private final ProjectRepository projectRepository;
    public SelectAssignmentCandidates(SquadAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }
    public void selectAssignmentCandidates(Long projectId, Long userId){
//        1) projectId로 프로젝트를 조회한다.
//        2) policy에 따라 후보 인력 계산한다
//        3) 계산된 후보인력을 '배정후보'로 생성한다
//        4) 배정 후보를 저장한다
        SquadAssignment assignment;

    }
}

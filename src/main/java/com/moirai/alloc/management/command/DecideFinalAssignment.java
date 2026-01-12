package com.moirai.alloc.management.command;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DecideFinalAssignment {
    public void decideFinalAssignment(Long assignmentId, FinalDecision decision){

    }

}

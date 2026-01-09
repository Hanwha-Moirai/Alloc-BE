package com.moirai.alloc.management.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "squad_assignment",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"project_id", "user_id"}
                )
        }
)
public class SquadAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime proposedAt;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus assignmentStatus;

    @Enumerated(EnumType.STRING)
    private FinalDecision finalDecision;

    private LocalDateTime decidedAt;

    protected SquadAssignment() {}
    // TODO 상태 변경 내부 로직 추가할 것(finaldecision, assignmentstatus)
}

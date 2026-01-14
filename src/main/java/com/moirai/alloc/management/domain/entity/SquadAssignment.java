package com.moirai.alloc.management.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(nullable = false)
    private AssignmentStatus assignmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinalDecision finalDecision;

    private LocalDateTime decidedAt;

    @Column(nullable = true)
    private Integer fitnessScore; // 적합도 저장할 메서드 필요함

    public static SquadAssignment propose(Long projectId, Long userId, int fitnessScore) {
        SquadAssignment sa = new SquadAssignment();
        sa.projectId = projectId;
        sa.userId = userId;
        sa.fitnessScore = fitnessScore;
        sa.proposedAt = LocalDateTime.now();
        // 선발되면 기본 상태
        sa.assignmentStatus = AssignmentStatus.REQUESTED;
        sa.finalDecision = FinalDecision.PENDING;
        return sa;
    }

    private void validateNotDecided() {
        if (this.finalDecision != FinalDecision.PENDING) {
            throw new IllegalStateException(
                    "이미 최종 결정된 인력 배치는 상태를 변경할 수 없습니다."
            );
        }
    }
    // 직원 액션
    // 직원 수락
    public void acceptAssignment() {
        validateNotDecided();
        if (this.assignmentStatus != AssignmentStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태에서만 수락할 수 있습니다.");
        }
        this.assignmentStatus = AssignmentStatus.ACCEPTED;
    }
    // 직원 인터뷰 요청
    public void requestInterview() {
        validateNotDecided();
        if (this.assignmentStatus != AssignmentStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태에서만 인터뷰 요청이 가능합니다.");
        }
        this.assignmentStatus = AssignmentStatus.INTERVIEW_REQUESTED;
    }
    // pm 액션
    public void finalAssign() {
        validateNotDecided();
        this.finalDecision = FinalDecision.ASSIGNED;
        this.decidedAt = LocalDateTime.now();
    }

    public void finalExclude() {
        validateNotDecided();
        this.finalDecision = FinalDecision.EXCLUDED;
        this.decidedAt = LocalDateTime.now();
    }
    //조회용
    public boolean isPending() {
        return this.finalDecision == FinalDecision.PENDING;
    }

    public boolean isFinallyAssigned() {
        return this.finalDecision == FinalDecision.ASSIGNED;
    }

}
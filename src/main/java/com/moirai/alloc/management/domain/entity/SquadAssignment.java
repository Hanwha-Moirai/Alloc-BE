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

    // PM 자동 배정용 팩토리 메서드; 프로젝트 생성시 AssignProjectManager에서 호출
    // 즉시 assigned 상태로 생성된다. pm은 프로젝트 생성과 동시 확정 참여자.
    public static SquadAssignment assignPm(Long projectId, Long pmUserId) {
        SquadAssignment sa = new SquadAssignment();
        sa.projectId = projectId;
        sa.userId = pmUserId;
        sa.proposedAt = LocalDateTime.now();

        // PM은 요청/응답 절차 없이 즉시 배정
        sa.assignmentStatus = AssignmentStatus.ACCEPTED;
        sa.finalDecision = FinalDecision.ASSIGNED;
        sa.decidedAt = LocalDateTime.now();

        // PM은 적합도 개념이 없으므로 null 유지
        sa.fitnessScore = null;

        return sa;
    }

    //후보 제안
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

    private void validateAssignedUser(Long actorUserId) {
        if (!this.userId.equals(actorUserId)) {
            throw new IllegalStateException("배정 대상자만 수행할 수 있는 작업입니다.");
        }
    }
    // 직원 액션
    // 직원 수락
    public void acceptAssignment(Long actorUserId) {
        validateAssignedUser(actorUserId);

        if (this.assignmentStatus != AssignmentStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태에서만 수락할 수 있습니다.");
        }

        this.assignmentStatus = AssignmentStatus.ACCEPTED;
        this.finalDecision = FinalDecision.ASSIGNED;
        this.decidedAt = LocalDateTime.now();
    }
    // 직원 인터뷰 요청
    public void requestInterview(Long actorUserId) {
        validateAssignedUser(actorUserId);

        if (this.assignmentStatus != AssignmentStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태에서만 인터뷰 요청이 가능합니다.");
        }

        this.assignmentStatus = AssignmentStatus.INTERVIEW_REQUESTED;
    }
    // pm 액션
    public void finalAssign() {
        validateInterviewRequested();
        this.finalDecision = FinalDecision.ASSIGNED;
        this.decidedAt = LocalDateTime.now();
    }

    public void finalExclude() {
        validateInterviewRequested();
        this.finalDecision = FinalDecision.EXCLUDED;
        this.decidedAt = LocalDateTime.now();
    }
    private void validateInterviewRequested() {
        if (this.assignmentStatus != AssignmentStatus.INTERVIEW_REQUESTED) {
            throw new IllegalStateException(
                    "인터뷰 요청 상태에서만 PM이 최종 결정을 할 수 있습니다."
            );
        }
    }
    //조회용
    public boolean isPending() {
        return this.finalDecision == FinalDecision.PENDING;
    }

    public boolean isFinallyAssigned() {
        return this.finalDecision == FinalDecision.ASSIGNED;
    }

}

package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.management.command.service.AssignProjectManager;
import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
// 목적: pm 자동 배정이라는 도메인 정책을 수행하여 sqaudassignment를 생헝하고 저장 요청하는지 검증
@ExtendWith(MockitoExtension.class)
class AssignProjectManagerTest {

    @InjectMocks
    private AssignProjectManager assignProjectManager;

    @Mock
    private SquadAssignmentRepository squadAssignmentRepository;

    @Test
    void projectCreatorIsSavedAsProjectManagerInSquadAssignment()
    {
        // =========================
        // given
        // =========================
        Long projectId = 100L;
        Long pmUserId = 1L;


        // when
        assignProjectManager.assignPm(projectId, pmUserId);

        // SquadAssignment가 생성되어 저장 요청이 발생하는지 검증
        ArgumentCaptor<SquadAssignment> captor =
                ArgumentCaptor.forClass(SquadAssignment.class);

        verify(squadAssignmentRepository).save(captor.capture());

        SquadAssignment assignment = captor.getValue();

        // 저장된 Assignment의 핵심 필드 검증
        assertThat(assignment.getProjectId()).isEqualTo(projectId);
        assertThat(assignment.getUserId()).isEqualTo(pmUserId);
        assertThat(assignment.getFinalDecision()).isEqualTo(FinalDecision.ASSIGNED);
    }
}

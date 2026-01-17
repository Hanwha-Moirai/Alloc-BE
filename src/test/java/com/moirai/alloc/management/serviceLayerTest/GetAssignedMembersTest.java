package com.moirai.alloc.management.serviceLayerTest;

import com.moirai.alloc.hr.command.domain.Department;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.selectedWorker.AssignedMemberDTO;
import com.moirai.alloc.management.query.service.GetAssignedMembers;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.user.command.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAssignedMembersTest {

    @Mock
    private SquadAssignmentRepository assignmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private GetAssignedMembers getAssignedMembers;

    private Employee createEmployee(Long userId, String userName, String jobName) {
        // User (이름의 주인)
        User user = User.builder()
                .loginId("login" + userId)
                .password("pw")
                .userName(userName)
                .email("test" + userId + "@test.com")
                .phone("010-0000-0000")
                .auth(User.Auth.USER)
                .build();

        // JobStandard (Builder 사용)
        JobStandard job = JobStandard.builder()
                .jobName(jobName)
                .build();

        // 필수 연관관계는 mock
        Department department = org.mockito.Mockito.mock(Department.class);
        TitleStandard titleStandard = org.mockito.Mockito.mock(TitleStandard.class);

        // Employee (Builder 사용)
        Employee employee = Employee.builder()
                .user(user)
                .job(job)
                .department(department)
                .titleStandard(titleStandard)
                .hiringDate(LocalDate.now())
                .build();

        // @MapsId 처리
        ReflectionTestUtils.setField(employee, "userId", userId);

        return employee;
    }



//     프로젝트에 최종 배정된 인력이 존재하는 경우
//     해당 인력들만 DTO로 변환되어 반환되는지 검증
    @Test
    void getAssignedMembersReturnsOnlyAssignedMembers() {
        // given
        Long projectId = 1L;

        SquadAssignment sa1 =
                SquadAssignment.propose(projectId, 10L, 80);
        sa1.finalAssign();

        SquadAssignment sa2 =
                SquadAssignment.propose(projectId, 20L, 90);
        sa2.finalAssign();

        given(assignmentRepository.findAssignedByProjectId(projectId))
                .willReturn(List.of(sa1, sa2));

        Employee e1 = createEmployee(10L, "김철수", "Backend");
        Employee e2 = createEmployee(20L, "이영희", "Frontend");

        given(employeeRepository.findAllByUserIdIn(List.of(10L, 20L)))
                .willReturn(List.of(e1, e2));

        // when
        List<AssignedMemberDTO> result =
                getAssignedMembers.getAssignedMembers(projectId);

        // then
        assertThat(result).hasSize(2);
    }
//     프로젝트에 최종 배정된 인력이 없는 경우
//     빈 리스트를 반환해야 하며
//     불필요한 Employee 조회가 발생하지 않아야 함
    @Test
    void getAssignedMembersReturnsEmptyListWhenNoneAssigned() {
        // given
        given(assignmentRepository.findAssignedByProjectId(1L))
                .willReturn(List.of());

        // when
        List<AssignedMemberDTO> result =
                getAssignedMembers.getAssignedMembers(1L);

        // then
        assertThat(result).isEmpty();
        verify(employeeRepository, never()).findAllByUserIdIn(any());
    }
}
package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.selectedWorker.AssignedMemberDTO;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAssignedMembers {
//1) 프로젝트 ID 기준으로 ASSIGNED 상태의 SquadAssignment 조회
//2) 배정된 userId 목록 추출
//3) userId 기준으로 Employee 정보 일괄 조회
//4) Assignment + Employee 정보를 조합하여 AssignedMemberDTO로 변환

    private final SquadAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;

    public List<AssignedMemberDTO> getAssignedMembers(Long projectId) {

        List<SquadAssignment> assignments =
                assignmentRepository.findAssignedByProjectId(projectId);

        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = assignments.stream()
                .map(SquadAssignment::getUserId)
                .toList();

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllByUserIdIn(userIds).stream()
                        .collect(Collectors.toMap(Employee::getUserId, e -> e));

        return assignments.stream()
                .map(sa -> {
                    Employee e = employeeMap.get(sa.getUserId());
                    return new AssignedMemberDTO(
                            sa.getUserId(),
                            e.getUser().getUserName(),
                            e.getJob().getJobName(),
                            projectId
                    );
                })
                .toList();
    }
}

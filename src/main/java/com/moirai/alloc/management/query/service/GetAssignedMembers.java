package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.AssignedMemberDTO;
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

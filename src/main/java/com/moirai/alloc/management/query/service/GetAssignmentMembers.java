package com.moirai.alloc.management.query.service;

import com.moirai.alloc.management.domain.entity.FinalDecision;
import com.moirai.alloc.management.domain.entity.SquadAssignment;
import com.moirai.alloc.management.domain.repo.SquadAssignmentRepository;
import com.moirai.alloc.management.query.dto.candidate_list.AssignmentCandidateItemDTO;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAssignmentMembers {
    //1) 선발된 인원 리스트업

    private final SquadAssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private String resolveMainSkill(Employee employee) {
        return employee.getSkills().stream()
                .max(Comparator.comparingInt(
                        skill -> skill.getProficiency().ordinal()
                ))
                .map(skill -> skill.getTech().getTechName())
                .orElse(null);
    }

    public List<AssignmentCandidateItemDTO> getMembers(Long projectId) {

        List<SquadAssignment> assignments =
                assignmentRepository.findByProjectId(projectId);

        List<Long> userIds = assignments.stream()
                .map(SquadAssignment::getUserId)
                .toList();

        Map<Long, User> userMap =
                userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Long, Employee> employeeMap =
                employeeRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(Employee::getUserId, e -> e));

        return assignments.stream()
                .map(a -> {
                    User u = userMap.get(a.getUserId());
                    Employee e = employeeMap.get(a.getUserId());

                    AssignmentCandidateItemDTO.WorkStatus workStatus =
                            a.getFinalDecision() == FinalDecision.ASSIGNED
                                    ? AssignmentCandidateItemDTO.WorkStatus.ASSIGNED
                                    : AssignmentCandidateItemDTO.WorkStatus.AVAILABLE;

                    return new AssignmentCandidateItemDTO(
                            u.getUserId(),
                            u.getUserName(),
                            e.getJob().getJobName(),
                            resolveMainSkill(e),
                            e.getTitleStandard().getMonthlyCost(),
                            workStatus,
                            a.getFitnessScore(),
                            true // 요청된 인원은 항상 selected
                    );
                })
                .toList();
    }
}

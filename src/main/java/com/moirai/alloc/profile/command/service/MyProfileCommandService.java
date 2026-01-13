package com.moirai.alloc.profile.command.service;

import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TechStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.hr.command.repository.TechStandardRepository;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill;
import com.moirai.alloc.profile.command.dto.response.MyProfileUpdateResponse;
import com.moirai.alloc.profile.command.dto.response.TechStackDeleteResponse;
import com.moirai.alloc.profile.command.dto.response.TechStackItemResponse;
import com.moirai.alloc.profile.command.dto.request.MyProfileUpdateRequest;
import com.moirai.alloc.profile.command.dto.request.TechStackCreateRequest;
import com.moirai.alloc.profile.command.dto.request.TechStackProficiencyUpdateRequest;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.profile.command.repository.EmployeeSkillRepository;
import com.moirai.alloc.user.command.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyProfileCommandService {

    private final EmployeeRepository employeeRepository;
    private final JobStandardRepository jobStandardRepository;
    private final EmployeeSkillRepository employeeSkillRepository;
    private final TechStandardRepository techStandardRepository;

    // 기본 정보 수정
    @Transactional
    public MyProfileUpdateResponse updateMyProfile(Long userId, MyProfileUpdateRequest req) {

        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE_NOT_FOUND"));

        User user = employee.getUser();

        // 1) 이메일/연락처 수정
        user.updateContact(req.getEmail(), req.getPhone());

        // 2) 직군 수정: jobId가 오면 "선택->변경" 처리 (초기엔 null → 선택하면 세팅)
        if (req.getJobId() != null) {
            JobStandard job = jobStandardRepository.findById(req.getJobId())
                    .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND"));
            employee.changeJob(job);
        }

        TitleStandard title = employee.getTitleStandard();

        return MyProfileUpdateResponse.builder()
                .userId(userId)
                .userName(user.getUserName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobId(employee.getJob() != null ? employee.getJob().getJobId() : null)
                .jobName(employee.getJob() != null ? employee.getJob().getJobName() : null)
                .titleId(title != null ? title.getTitleStandardId() : null)
                .titleName(title != null ? title.getTitleName() : null)
                .build();
    }

    // 기술 스택 등록
    @Transactional
    public TechStackItemResponse createTechStack(Long userId, TechStackCreateRequest req) {

        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE_NOT_FOUND"));

        TechStandard tech = techStandardRepository.findById(req.getTechId())
                .orElseThrow(() -> new IllegalArgumentException("TECH_NOT_FOUND"));

        // 중복 등록 방지
        if (employeeSkillRepository.existsByEmployee_UserIdAndTech_TechId(userId, req.getTechId())) {
            throw new IllegalStateException("DUPLICATE_TECH_STACK");
        }

        EmployeeSkill saved = employeeSkillRepository.save(
                EmployeeSkill.builder()
                        .employee(employee)
                        .tech(tech)
                        .proficiency(req.getProficiency()) // null이면 LV1
                        .build()
        );

        return TechStackItemResponse.builder()
                .employeeTechId(saved.getEmployeeTechId())
                .techId(tech.getTechId())
                .techName(tech.getTechName())
                .proficiency(saved.getProficiency())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    // 기술 스택 숙련도 수정
    @Transactional
    public TechStackItemResponse updateProficiency(Long userId, Long employeeTechId,
                                                   TechStackProficiencyUpdateRequest req) {

        EmployeeSkill skill = employeeSkillRepository.findById(employeeTechId)
                .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE_TECH_NOT_FOUND"));

        // 본인 기술 스택인지 검증
        if (!skill.getEmployee().getUserId().equals(userId)) {
            throw new AccessDeniedException("FORBIDDEN_TECH_STACK");
        }

        skill.changeProficiency(req.getProficiency());

        TechStandard tech = skill.getTech();

        return TechStackItemResponse.builder()
                .employeeTechId(skill.getEmployeeTechId())
                .techId(tech.getTechId())
                .techName(tech.getTechName())
                .proficiency(skill.getProficiency())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    // 기술 스택 삭제
    @Transactional
    public TechStackDeleteResponse deleteTechStack(Long userId, Long employeeTechId) {

        EmployeeSkill skill = employeeSkillRepository.findById(employeeTechId)
                .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE_TECH_NOT_FOUND"));

        // 본인 기술 스택인지 검증
        if (!skill.getEmployee().getUserId().equals(userId)) {
            throw new AccessDeniedException("FORBIDDEN_TECH_STACK");
        }

        employeeSkillRepository.delete(skill);

        return TechStackDeleteResponse.builder()
                .employeeTechId(employeeTechId)
                .deleted(true)
                .build();
    }
}

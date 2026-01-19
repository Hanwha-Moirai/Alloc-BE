package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.admin.command.dto.request.AdminUserCreateRequest;
import com.moirai.alloc.admin.command.dto.request.AdminUserUpdateRequest;
import com.moirai.alloc.admin.command.dto.response.AdminUserResponse;
import com.moirai.alloc.hr.command.domain.Department;
import com.moirai.alloc.hr.command.domain.JobStandard;
import com.moirai.alloc.hr.command.domain.TitleStandard;
import com.moirai.alloc.hr.command.repository.DepartmentRepository;
import com.moirai.alloc.hr.command.repository.JobStandardRepository;
import com.moirai.alloc.hr.command.repository.TitleStandardRepository;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.profile.command.repository.EmployeeRepository;
import com.moirai.alloc.user.command.domain.User;
import com.moirai.alloc.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserCommandService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    private final JobStandardRepository jobStandardRepository;
    private final DepartmentRepository departmentRepository;
    private final TitleStandardRepository titleStandardRepository;

    private final PasswordEncoder passwordEncoder;

    public AdminUserResponse createUser(AdminUserCreateRequest req) {

        // 1) 중복 검증
        if (userRepository.existsByLoginId(req.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 로그인 ID 입니다.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }

        // 2) 기준 데이터 조회 (직군/부서/직급)
        JobStandard job = jobStandardRepository.findById(req.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND"));

        Department dept = departmentRepository.findById(req.getDeptId())
                .orElseThrow(() -> new IllegalArgumentException("DEPT_NOT_FOUND"));

        TitleStandard title = titleStandardRepository.findById(req.getTitleStandardId())
                .orElseThrow(() -> new IllegalArgumentException("TITLE_NOT_FOUND"));

        // 3) User 생성
        User user = userRepository.save(
                User.builder()
                        .loginId(req.getLoginId().trim())
                        .password(passwordEncoder.encode(req.getPassword()))
                        .userName(req.getUserName())
                        .birthday(req.getBirthday())
                        .email(req.getEmail())
                        .phone(req.getPhone())
                        .auth(req.getAuth())
                        .profileImg(req.getProfileImg())
                        .build()
        );

        // 4) Employee 생성
        Employee employee = Employee.builder()
                .user(user)
                .job(job)
                .department(dept)
                .titleStandard(title)
                .employeeType(req.getEmployeeType()) // null이면 FULL_TIME
                .hiringDate(
                        req.getHiringDate() != null ? req.getHiringDate() : LocalDate.now()
                )
                .build();

        employeeRepository.save(employee);

        return toResponse(user, employee);
    }

    public AdminUserResponse updateUser(Long userId, AdminUserUpdateRequest req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        Employee employee = employeeRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("EMPLOYEE_NOT_FOUND"));

        // 1) 이메일 중복 체크
        if (req.getEmail() != null && userRepository.existsByEmailAndUserIdNot(req.getEmail(), userId)) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }

        // 2) User 변경(들어온 것만)
        user.changeBasicInfo(
                req.getUserName(),
                req.getBirthday(),
                req.getEmail(),
                req.getPhone(),
                req.getProfileImg()
        );

        if (req.getPassword() != null) {
            user.changePassword(passwordEncoder.encode(req.getPassword()));
        }
        user.changeAuth(req.getAuth());
        user.changeStatus(req.getStatus());

        // 3) HR 변경(들어온 것만)
        if (req.getJobId() != null) {
            JobStandard job = jobStandardRepository.findById(req.getJobId())
                    .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND"));
            employee.changeJob(job);
        }
        if (req.getDeptId() != null) {
            Department dept = departmentRepository.findById(req.getDeptId())
                    .orElseThrow(() -> new IllegalArgumentException("DEPT_NOT_FOUND"));
            employee.changeDepartment(dept);
        }
        if (req.getTitleStandardId() != null) {
            TitleStandard title = titleStandardRepository.findById(req.getTitleStandardId())
                    .orElseThrow(() -> new IllegalArgumentException("TITLE_NOT_FOUND"));
            employee.changeTitleStandard(title);
        }
        if (req.getEmployeeType() != null) {
            employee.changeEmployeeType(req.getEmployeeType());
        }

        return toResponse(user, employee);
    }

    private AdminUserResponse toResponse(User user, Employee employee) {
        JobStandard job = employee.getJob();
        Department dept = employee.getDepartment();
        TitleStandard title = employee.getTitleStandard();

        return AdminUserResponse.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .userName(user.getUserName())
                .birthday(user.getBirthday())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileImg(user.getProfileImg())
                .auth(user.getAuth())
                .status(user.getStatus())
                .jobId(job != null ? job.getJobId() : null)
                .jobName(job != null ? job.getJobName() : null)
                .deptId(dept != null ? dept.getDeptId() : null)
                .deptName(dept != null ? dept.getDeptName() : null)
                .titleStandardId(title != null ? title.getTitleStandardId() : null)
                .titleName(title != null ? title.getTitleName() : null)
                .build();
    }
}

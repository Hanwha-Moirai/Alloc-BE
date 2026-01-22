package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.admin.command.dto.request.AdminUserCreateRequest;
import com.moirai.alloc.admin.command.dto.request.AdminUserUpdateRequest;
import com.moirai.alloc.admin.command.dto.response.AdminUserResponse;
import com.moirai.alloc.profile.command.domain.entity.Employee;
import com.moirai.alloc.user.command.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/user_setup.sql")
class AdminUserCommandServiceTest {

    @Autowired
    private AdminUserCommandService adminUserCommandService;

    // user_setup.sql 기준
    private static final Long USER_ID_KMJ = 77001L;
    private static final Long USER_ID_OTHER = 77002L;

    private static final Long JOB_ID_BACKEND = 1L;
    private static final Long DEPT_ID_DEV = 1L;
    private static final Long TITLE_ID_JUNIOR = 1L;

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    @Nested
    @DisplayName("사용자 생성")
    class CreateUser {

        @Test
        @DisplayName("사용자를 생성한다")
        void createUser_success() {
            AdminUserCreateRequest req = new AdminUserCreateRequest();
            setField(req, "loginId", "newuser");
            setField(req, "password", "password1234");
            setField(req, "userName", "신규사용자");
            setField(req, "birthday", LocalDate.of(1999, 1, 1));
            setField(req, "email", "newuser@alloc.co.kr");
            setField(req, "phone", "010-1111-2222");
            setField(req, "auth", User.Auth.USER);

            setField(req, "jobId", JOB_ID_BACKEND);
            setField(req, "deptId", DEPT_ID_DEV);
            setField(req, "titleStandardId", TITLE_ID_JUNIOR);

            setField(req, "profileImg", "https://img.test/1.png");
            setField(req, "employeeType", Employee.EmployeeType.FULL_TIME);
            setField(req, "hiringDate", LocalDate.of(2025, 1, 1));

            AdminUserResponse res = adminUserCommandService.createUser(req);

            assertThat(res.getUserId()).isNotNull();
            assertThat(res.getLoginId()).isEqualTo("newuser");
            assertThat(res.getUserName()).isEqualTo("신규사용자");
            assertThat(res.getEmail()).isEqualTo("newuser@alloc.co.kr");
            assertThat(res.getPhone()).isEqualTo("010-1111-2222");
            assertThat(res.getBirthday()).isEqualTo(LocalDate.of(1999, 1, 1));
            assertThat(res.getAuth()).isEqualTo(User.Auth.USER);

            assertThat(res.getJobId()).isEqualTo(JOB_ID_BACKEND);
            assertThat(res.getDeptId()).isEqualTo(DEPT_ID_DEV);
            assertThat(res.getTitleStandardId()).isEqualTo(TITLE_ID_JUNIOR);

            // jobName/deptName/titleName은 setup.sql 값 기반
            assertThat(res.getJobName()).isEqualTo("BackendDeveloper");
            assertThat(res.getDeptName()).isEqualTo("Dev");
            assertThat(res.getTitleName()).isEqualTo("Junior");
        }

        @Test
        @DisplayName("loginId가 중복이면 예외를 발생시킨다")
        void duplicateLoginId_throwsException() {
            AdminUserCreateRequest req = new AdminUserCreateRequest();
            setField(req, "loginId", "kmj");
            setField(req, "password", "password1234");
            setField(req, "userName", "중복");
            setField(req, "birthday", LocalDate.of(1999, 1, 1));
            setField(req, "email", "unique-email@alloc.co.kr");
            setField(req, "phone", "010-1111-2222");
            setField(req, "auth", User.Auth.USER);

            setField(req, "jobId", JOB_ID_BACKEND);
            setField(req, "deptId", DEPT_ID_DEV);
            setField(req, "titleStandardId", TITLE_ID_JUNIOR);
            setField(req, "employeeType", Employee.EmployeeType.FULL_TIME);
            setField(req, "hiringDate", LocalDate.of(2025, 1, 1));

            assertThatThrownBy(() -> adminUserCommandService.createUser(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 로그인 ID 입니다.");
        }

        @Test
        @DisplayName("email이 중복이면 예외를 발생시킨다")
        void duplicateEmail_throwsException() {
            AdminUserCreateRequest req = new AdminUserCreateRequest();
            setField(req, "loginId", "unique-login");
            setField(req, "password", "password1234");
            setField(req, "userName", "중복이메일");
            setField(req, "birthday", LocalDate.of(1999, 1, 1));
            setField(req, "email", "kmj@alloc.co.kr"); // setup.sql에 이미 존재
            setField(req, "phone", "010-1111-2222");
            setField(req, "auth", User.Auth.USER);

            setField(req, "jobId", JOB_ID_BACKEND);
            setField(req, "deptId", DEPT_ID_DEV);
            setField(req, "titleStandardId", TITLE_ID_JUNIOR);
            setField(req, "employeeType", Employee.EmployeeType.FULL_TIME);
            setField(req, "hiringDate", LocalDate.of(2025, 1, 1));

            assertThatThrownBy(() -> adminUserCommandService.createUser(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 이메일 입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 JOB이면 예외를 발생시킨다")
        void jobNotFound_throwsException() {
            AdminUserCreateRequest req = new AdminUserCreateRequest();
            setField(req, "loginId", "newuser2");
            setField(req, "password", "password1234");
            setField(req, "userName", "신규사용자2");
            setField(req, "birthday", LocalDate.of(1999, 1, 1));
            setField(req, "email", "newuser2@alloc.co.kr");
            setField(req, "phone", "010-1111-2222");
            setField(req, "auth", User.Auth.USER);

            setField(req, "jobId", 999999L); // 존재 X
            setField(req, "deptId", DEPT_ID_DEV);
            setField(req, "titleStandardId", TITLE_ID_JUNIOR);
            setField(req, "employeeType", Employee.EmployeeType.FULL_TIME);
            setField(req, "hiringDate", LocalDate.of(2025, 1, 1));

            assertThatThrownBy(() -> adminUserCommandService.createUser(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JOB_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class UpdateUser {

        @Test
        @DisplayName("사용자 기본 정보를 수정한다")
        void updateUser_basic_success() {
            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "userName", "변경된이름");
            setField(req, "email", "changed@alloc.co.kr");
            setField(req, "phone", "010-9999-8888");
            setField(req, "birthday", LocalDate.of(1998, 12, 31));
            setField(req, "profileImg", "https://img.test/changed.png");

            AdminUserResponse res = adminUserCommandService.updateUser(USER_ID_KMJ, req);

            assertThat(res.getUserId()).isEqualTo(USER_ID_KMJ);
            assertThat(res.getUserName()).isEqualTo("변경된이름");
            assertThat(res.getEmail()).isEqualTo("changed@alloc.co.kr");
            assertThat(res.getPhone()).isEqualTo("010-9999-8888");
            assertThat(res.getBirthday()).isEqualTo(LocalDate.of(1998, 12, 31));
            assertThat(res.getProfileImg()).isEqualTo("https://img.test/changed.png");
        }

        @Test
        @DisplayName("HR 정보를 수정한다(직군/부서/직급/고용형태)")
        void updateUser_hr_success() {
            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "jobId", JOB_ID_BACKEND);
            setField(req, "deptId", DEPT_ID_DEV);
            setField(req, "titleStandardId", TITLE_ID_JUNIOR);
            setField(req, "employeeType", Employee.EmployeeType.FULL_TIME);

            AdminUserResponse res = adminUserCommandService.updateUser(USER_ID_KMJ, req);

            assertThat(res.getJobId()).isEqualTo(JOB_ID_BACKEND);
            assertThat(res.getDeptId()).isEqualTo(DEPT_ID_DEV);
            assertThat(res.getTitleStandardId()).isEqualTo(TITLE_ID_JUNIOR);

            assertThat(res.getJobName()).isEqualTo("BackendDeveloper");
            assertThat(res.getDeptName()).isEqualTo("Dev");
            assertThat(res.getTitleName()).isEqualTo("Junior");
        }

        @Test
        @DisplayName("auth/status를 수정한다")
        void updateUser_auth_status_success() {
            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "auth", User.Auth.ADMIN);
            setField(req, "status", User.Status.ACTIVE);

            AdminUserResponse res = adminUserCommandService.updateUser(USER_ID_KMJ, req);

            assertThat(res.getAuth()).isEqualTo(User.Auth.ADMIN);
            assertThat(res.getStatus()).isEqualTo(User.Status.ACTIVE);
        }

        @Test
        @DisplayName("다른 사용자 이메일과 중복이면 예외를 발생시킨다")
        void duplicateEmail_throwsException() {
            // USER_ID_KMJ가, USER_ID_OTHER의 이메일(other@alloc.co.kr)로 변경 시도 -> 중복
            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "email", "other@alloc.co.kr");

            assertThatThrownBy(() -> adminUserCommandService.updateUser(USER_ID_KMJ, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 이메일 입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 userId면 예외를 발생시킨다(USER_NOT_FOUND)")
        void userNotFound_throwsException() {
            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "userName", "수정");

            assertThatThrownBy(() -> adminUserCommandService.updateUser(999999L, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("USER_NOT_FOUND");
        }

        @Test
        @DisplayName("Employee가 없으면 예외를 발생시킨다(EMPLOYEE_NOT_FOUND)")
        void employeeNotFound_throwsException() {
            // 이 케이스를 만들려면 DB에 "user만 있고 employee가 없는" userId가 필요함.
            // user_setup.sql에 user_id=88001 같은 row를 추가하고 employee는 안 넣는 식으로 구성하면 됨.
            Long USER_ID_ONLY_USER = 88001L;

            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "userName", "수정");

            assertThatThrownBy(() -> adminUserCommandService.updateUser(USER_ID_ONLY_USER, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EMPLOYEE_NOT_FOUND");
        }

        @Test
        @DisplayName("존재하지 않는 직군이면 예외를 발생시킨다(JOB_NOT_FOUND)")
        void jobNotFound_throwsException() {
            AdminUserUpdateRequest req = new AdminUserUpdateRequest();
            setField(req, "jobId", 999999L);

            assertThatThrownBy(() -> adminUserCommandService.updateUser(USER_ID_KMJ, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JOB_NOT_FOUND");
        }
    }
}

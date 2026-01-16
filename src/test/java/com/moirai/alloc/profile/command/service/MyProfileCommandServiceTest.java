package com.moirai.alloc.profile.command.service;

import com.moirai.alloc.profile.command.domain.entity.EmployeeSkill.Proficiency;
import com.moirai.alloc.profile.command.dto.request.MyProfileUpdateRequest;
import com.moirai.alloc.profile.command.dto.request.TechStackCreateRequest;
import com.moirai.alloc.profile.command.dto.request.TechStackProficiencyUpdateRequest;
import com.moirai.alloc.profile.command.dto.response.MyProfileUpdateResponse;
import com.moirai.alloc.profile.command.dto.response.TechStackDeleteResponse;
import com.moirai.alloc.profile.command.dto.response.TechStackItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
//@Sql(scripts = "/sql/profile/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
//@Sql(scripts = "/sql/profile/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MyProfileCommandServiceTest {

    private static final Long USER_ID_KMJ = 77001L;
    private static final Long USER_ID_NO_STACK = 77002L;
    private static final Long EMPLOYEE_TECH_ID_JAVA = 1001L;
    private static final Long TECH_ID_JAVA = 1L;
    private static final Long TECH_ID_SPRING = 2L;
    private static final Long JOB_ID_BACKEND = 1L;

    @Autowired
    private MyProfileCommandService myProfileCommandService;

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
    @DisplayName("기본 정보 수정")
    class UpdateMyProfile {

        @Test
        @DisplayName("이메일과 연락처를 수정한다")
        void updateContact_success() {
            MyProfileUpdateRequest req = new MyProfileUpdateRequest();
            setField(req, "email", "new-email@alloc.co.kr");
            setField(req, "phone", "010-9999-8888");

            MyProfileUpdateResponse res = myProfileCommandService.updateMyProfile(USER_ID_KMJ, req);

            assertThat(res.getUserId()).isEqualTo(USER_ID_KMJ);
            assertThat(res.getEmail()).isEqualTo("new-email@alloc.co.kr");
            assertThat(res.getPhone()).isEqualTo("010-9999-8888");
            assertThat(res.getUserName()).isEqualTo("김명진");
        }

        @Test
        @DisplayName("이메일만 수정한다")
        void updateEmailOnly_success() {
            MyProfileUpdateRequest req = new MyProfileUpdateRequest();
            setField(req, "email", "only-email@alloc.co.kr");

            MyProfileUpdateResponse res = myProfileCommandService.updateMyProfile(USER_ID_KMJ, req);

            assertThat(res.getEmail()).isEqualTo("only-email@alloc.co.kr");
        }

        @Test
        @DisplayName("직군을 수정한다")
        void updateJob_success() {
            MyProfileUpdateRequest req = new MyProfileUpdateRequest();
            setField(req, "jobId", JOB_ID_BACKEND);

            MyProfileUpdateResponse res = myProfileCommandService.updateMyProfile(USER_ID_KMJ, req);

            assertThat(res.getJobId()).isEqualTo(JOB_ID_BACKEND);
            assertThat(res.getJobName()).isEqualTo("BackendDeveloper");
        }

        @Test
        @DisplayName("변경 사항이 없으면 예외를 발생시킨다")
        void noChanges_throwsException() {
            MyProfileUpdateRequest req = new MyProfileUpdateRequest();

            assertThatThrownBy(() -> myProfileCommandService.updateMyProfile(USER_ID_KMJ, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("NO_CHANGES");
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 예외를 발생시킨다")
        void userNotFound_throwsException() {
            MyProfileUpdateRequest req = new MyProfileUpdateRequest();
            setField(req, "email", "test@test.com");

            assertThatThrownBy(() -> myProfileCommandService.updateMyProfile(999999L, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EMPLOYEE_NOT_FOUND");
        }

        @Test
        @DisplayName("존재하지 않는 직군이면 예외를 발생시킨다")
        void jobNotFound_throwsException() {
            MyProfileUpdateRequest req = new MyProfileUpdateRequest();
            setField(req, "jobId", 999999L);

            assertThatThrownBy(() -> myProfileCommandService.updateMyProfile(USER_ID_KMJ, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("JOB_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("기술 스택 등록")
    class CreateTechStack {

        @Test
        @DisplayName("기술 스택을 등록한다")
        void createTechStack_success() {
            TechStackCreateRequest req = new TechStackCreateRequest();
            setField(req, "techId", TECH_ID_JAVA);
            setField(req, "proficiency", Proficiency.LV2);

            TechStackItemResponse res = myProfileCommandService.createTechStack(USER_ID_NO_STACK, req);

            assertThat(res.getTechId()).isEqualTo(TECH_ID_JAVA);
            assertThat(res.getTechName()).isEqualTo("Java");
            assertThat(res.getProficiency()).isEqualTo(Proficiency.LV2);
            assertThat(res.getEmployeeTechId()).isNotNull();
        }

        @Test
        @DisplayName("숙련도 미지정 시 기본값 LV1로 등록한다")
        void createTechStack_defaultProficiency() {
            TechStackCreateRequest req = new TechStackCreateRequest();
            setField(req, "techId", TECH_ID_SPRING);

            TechStackItemResponse res = myProfileCommandService.createTechStack(USER_ID_NO_STACK, req);

            assertThat(res.getProficiency()).isEqualTo(Proficiency.LV1);
        }

        @Test
        @DisplayName("이미 등록된 기술이면 예외를 발생시킨다")
        void duplicateTech_throwsException() {
            TechStackCreateRequest req = new TechStackCreateRequest();
            setField(req, "techId", TECH_ID_JAVA);
            setField(req, "proficiency", Proficiency.LV3);

            assertThatThrownBy(() -> myProfileCommandService.createTechStack(USER_ID_KMJ, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("DUPLICATE_TECH_STACK");
        }

        @Test
        @DisplayName("존재하지 않는 기술이면 예외를 발생시킨다")
        void techNotFound_throwsException() {
            TechStackCreateRequest req = new TechStackCreateRequest();
            setField(req, "techId", 999999L);
            setField(req, "proficiency", Proficiency.LV1);

            assertThatThrownBy(() -> myProfileCommandService.createTechStack(USER_ID_KMJ, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TECH_NOT_FOUND");
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 예외를 발생시킨다")
        void employeeNotFound_throwsException() {
            TechStackCreateRequest req = new TechStackCreateRequest();
            setField(req, "techId", TECH_ID_JAVA);

            assertThatThrownBy(() -> myProfileCommandService.createTechStack(999999L, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EMPLOYEE_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("기술 스택 숙련도 수정")
    class UpdateProficiency {

        @Test
        @DisplayName("숙련도를 수정한다")
        void updateProficiency_success() {
            TechStackProficiencyUpdateRequest req = new TechStackProficiencyUpdateRequest();
            setField(req, "proficiency", Proficiency.LV3);

            TechStackItemResponse res = myProfileCommandService.updateProficiency(
                    USER_ID_KMJ, EMPLOYEE_TECH_ID_JAVA, req);

            assertThat(res.getEmployeeTechId()).isEqualTo(EMPLOYEE_TECH_ID_JAVA);
            assertThat(res.getProficiency()).isEqualTo(Proficiency.LV3);
            assertThat(res.getTechName()).isEqualTo("Java");
        }

        @Test
        @DisplayName("다른 사용자의 기술 스택 수정 시 예외를 발생시킨다")
        void forbidden_throwsException() {
            TechStackProficiencyUpdateRequest req = new TechStackProficiencyUpdateRequest();
            setField(req, "proficiency", Proficiency.LV3);

            assertThatThrownBy(() -> myProfileCommandService.updateProficiency(
                    USER_ID_NO_STACK, EMPLOYEE_TECH_ID_JAVA, req))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("FORBIDDEN_TECH_STACK");
        }

        @Test
        @DisplayName("존재하지 않는 기술 스택이면 예외를 발생시킨다")
        void notFound_throwsException() {
            TechStackProficiencyUpdateRequest req = new TechStackProficiencyUpdateRequest();
            setField(req, "proficiency", Proficiency.LV3);

            assertThatThrownBy(() -> myProfileCommandService.updateProficiency(
                    USER_ID_KMJ, 999999L, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EMPLOYEE_TECH_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("기술 스택 삭제")
    class DeleteTechStack {

        @Test
        @DisplayName("기술 스택을 삭제한다")
        void deleteTechStack_success() {
            TechStackDeleteResponse res = myProfileCommandService.deleteTechStack(
                    USER_ID_KMJ, EMPLOYEE_TECH_ID_JAVA);

            assertThat(res.getEmployeeTechId()).isEqualTo(EMPLOYEE_TECH_ID_JAVA);
            assertThat(res.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("다른 사용자의 기술 스택 삭제 시 예외를 발생시킨다")
        void forbidden_throwsException() {
            assertThatThrownBy(() -> myProfileCommandService.deleteTechStack(
                    USER_ID_NO_STACK, EMPLOYEE_TECH_ID_JAVA))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("FORBIDDEN_TECH_STACK");
        }

        @Test
        @DisplayName("존재하지 않는 기술 스택이면 예외를 발생시킨다")
        void notFound_throwsException() {
            assertThatThrownBy(() -> myProfileCommandService.deleteTechStack(USER_ID_KMJ, 999999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("EMPLOYEE_TECH_NOT_FOUND");
        }
    }
}
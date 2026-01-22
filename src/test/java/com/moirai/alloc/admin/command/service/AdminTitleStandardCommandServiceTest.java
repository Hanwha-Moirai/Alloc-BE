package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/title_standard_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTitleStandardCommandServiceTest {

    @Autowired
    private AdminTitleStandardCommandService service;

    // title_setup.sql 기준
    private static final Long TITLE_ID_JUNIOR = 88001L;
    private static final Long TITLE_ID_SENIOR = 88002L;

    @Nested
    @DisplayName("직급 등록(createTitle)")
    class CreateTitle {

        @Test
        @DisplayName("직급을 등록한다")
        void createTitle_success() {
            Long id = service.createTitle("Lead", 3000000);

            assertThat(id).isNotNull();
        }

        @Test
        @DisplayName("직급명 앞뒤 공백은 trim되어 저장된다(중복 검사도 trim 기준)")
        void createTitle_trimmed() {
            Long id = service.createTitle("  Lead  ", 3000000);

            assertThat(id).isNotNull();
        }

        @Test
        @DisplayName("이미 존재하는 직급이면 예외를 발생시킨다")
        void createTitle_duplicate_throwsException() {
            assertThatThrownBy(() -> service.createTitle("Junior", 999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 직급입니다.");
        }

        @Test
        @DisplayName("직급명이 null이면 예외를 발생시킨다")
        void createTitle_nullName_throwsException() {
            assertThatThrownBy(() -> service.createTitle(null, 1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("직급 이름이 필요합니다.");
        }

        @Test
        @DisplayName("직급명이 공백이면 예외를 발생시킨다")
        void createTitle_blankName_throwsException() {
            assertThatThrownBy(() -> service.createTitle("   ", 1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("직급 이름이 필요합니다.");
        }
    }

    @Nested
    @DisplayName("직급 수정(updateTitle)")
    class UpdateTitle {

        @Test
        @DisplayName("직급을 수정한다")
        void updateTitle_success() {
            Long id = service.updateTitle(TITLE_ID_JUNIOR, "Junior", 1500000);

            assertThat(id).isEqualTo(TITLE_ID_JUNIOR);
        }

        @Test
        @DisplayName("존재하지 않는 직급이면 NotFoundException을 발생시킨다")
        void updateTitle_notFound_throwsException() {
            assertThatThrownBy(() -> service.updateTitle(999999L, "Ghost", 0))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("직급을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("다른 직급이 이미 같은 이름을 가지고 있으면 예외를 발생시킨다")
        void updateTitle_duplicate_throwsException() {
            // Junior(88001)을 Senior로 바꾸면 duplicated 로직에 걸림
            assertThatThrownBy(() -> service.updateTitle(TITLE_ID_JUNIOR, "Senior", 123))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 존재하는 직급입니다.");
        }

        @Test
        @DisplayName("직급명 null이면 예외를 발생시킨다")
        void updateTitle_nullName_throwsException() {
            assertThatThrownBy(() -> service.updateTitle(TITLE_ID_JUNIOR, null, 1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("직급 이름이 필요합니다.");
        }

        @Test
        @DisplayName("직급명 공백이면 예외를 발생시킨다")
        void updateTitle_blankName_throwsException() {
            assertThatThrownBy(() -> service.updateTitle(TITLE_ID_JUNIOR, "   ", 1000))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("직급 이름이 필요합니다.");
        }
    }
}

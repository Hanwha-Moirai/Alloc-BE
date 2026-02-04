package com.moirai.alloc.admin.command.service;

import com.moirai.alloc.hr.command.domain.TechStandard;
import com.moirai.alloc.hr.command.repository.TechStandardRepository;
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
import org.springframework.test.context.web.ServletTestExecutionListener;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        ServletTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@Sql(scripts = "/sql/admin/tech_stack_setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTechStackCommandServiceTest {

    @Autowired
    private AdminTechStackCommandService adminTechStackCommandService;

    @Autowired
    private TechStandardRepository techStandardRepository;

    @Test
    void createTechStack_savesTechStandard() {
        Long techId = adminTechStackCommandService.createTechStack("Kotlin");

        TechStandard techStandard = techStandardRepository.findById(techId).orElseThrow();
        assertThat(techStandard.getTechName()).isEqualTo("Kotlin");
    }

    @Test
    void updateTechStack_updatesTechName() {
        Long techId = adminTechStackCommandService.updateTechStack(99001L, "Java SE");

        TechStandard techStandard = techStandardRepository.findById(techId).orElseThrow();
        assertThat(techStandard.getTechName()).isEqualTo("Java SE");
    }

    @Test
    void deleteTechStack_removesTechStandard() {
        Long techId = adminTechStackCommandService.deleteTechStack(99002L);

        assertThat(techStandardRepository.findById(techId)).isEmpty();
    }
}

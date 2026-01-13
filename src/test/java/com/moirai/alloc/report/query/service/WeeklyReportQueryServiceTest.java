package com.moirai.alloc.report.query.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/report/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
//@Sql(scripts = "/sql/report/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@EnableJpaAuditing
class WeeklyReportQueryServiceTest {

    private static final Long REPORT_ID = 77001L;
    private static final Long PROJECT_ID = 77001L;
    private static final Long PM_USER_ID = 77001L;

    @Autowired
    private WeeklyReportQueryService weeklyReportQueryService;

    @Test
    void getDocsReports_returnsPage() {
        Page<WeeklyReportSummaryResponse> page =
                weeklyReportQueryService.getDocsReports(PROJECT_ID, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void searchDocsReports_filtersByKeyword() {
        WeeklyReportSearchCondition condition = new WeeklyReportSearchCondition(
                PROJECT_ID,
                null,
                null,
                null,
                null,
                "Report Project"
        );

        Page<WeeklyReportSummaryResponse> page =
                weeklyReportQueryService.searchDocsReports(PROJECT_ID, condition, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void getDocsReportDetail_returnsTasks() {
        WeeklyReportDetailResponse detail = weeklyReportQueryService.getDocsReportDetail(PROJECT_ID, REPORT_ID);

        assertThat(detail.completedTasks()).hasSize(1);
        assertThat(detail.incompleteTasks()).hasSize(1);
        assertThat(detail.nextWeekTasks()).hasSize(1);
    }

    @Test
    void getMyDocsReportDetail_checksMembership() {
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_77001",
                "pm77001@example.com",
                "PM",
                "PM",
                "pw"
        );

        WeeklyReportDetailResponse detail = weeklyReportQueryService.getMyDocsReportDetail(principal, REPORT_ID);

        assertThat(detail.reportId()).isEqualTo(REPORT_ID);
    }
}

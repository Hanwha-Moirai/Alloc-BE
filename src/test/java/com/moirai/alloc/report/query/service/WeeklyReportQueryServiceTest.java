package com.moirai.alloc.report.query.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportMissingResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@Sql(scripts = "/sql/report/setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/report/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class WeeklyReportQueryServiceTest {

    private static final Long REPORT_ID = 77001L;
    private static final Long PROJECT_ID = 77001L;
    private static final Long PM_USER_ID = 77001L;

    @Autowired
    private WeeklyReportQueryService weeklyReportQueryService;

    @Test
    @DisplayName("프로젝트 주간보고 목록을 페이지로 조회한다.")
    void getDocsReports_returnsPage() {
        Page<WeeklyReportSummaryResponse> page =
                weeklyReportQueryService.getDocsReports(PROJECT_ID, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("주간보고 검색 시 프로젝트 조건이 적용된다.")
    void searchDocsReports_filtersByProjectId() {
        WeeklyReportSearchCondition condition = new WeeklyReportSearchCondition(
                PROJECT_ID,
                null,
                null,
                null,
                null
        );

        Page<WeeklyReportSummaryResponse> page =
                weeklyReportQueryService.searchDocsReports(PROJECT_ID, condition, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("주간보고 상세 조회 시 태스크와 주차 정보를 반환한다.")
    void getDocsReportDetail_returnsTasks() {
        WeeklyReportDetailResponse detail = weeklyReportQueryService.getDocsReportDetail(PROJECT_ID, REPORT_ID);

        assertThat(detail.completedTasks()).hasSize(1);
        assertThat(detail.incompleteTasks()).hasSize(1);
        assertThat(detail.nextWeekTasks()).hasSize(1);
        assertThat(detail.reporterName()).isEqualTo("PM User");
        assertThat(detail.weekLabel()).isEqualTo("2025년 1월 2주차");
        assertThat(detail.incompleteTasks().get(0).delayedDates()).isEqualTo(6);
    }

    @Test
    @DisplayName("내 주간보고 상세 조회 시 멤버십을 검증한다.")
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

    @Test
    @DisplayName("작성하지 않은 주차 목록을 반환한다.")
    void getMissingWeeks_returnsMissingWeeks() {
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_77001",
                "pm77001@example.com",
                "PM",
                "PM",
                "pw"
        );

        List<WeeklyReportMissingResponse> missing =
                weeklyReportQueryService.getMissingWeeks(
                        principal,
                        PROJECT_ID,
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2025, 1, 31)
                );

        assertThat(missing).hasSize(5);
        assertThat(missing)
                .extracting(WeeklyReportMissingResponse::weekStartDate)
                .contains(
                        LocalDate.of(2024, 12, 29),
                        LocalDate.of(2025, 1, 5),
                        LocalDate.of(2025, 1, 12),
                        LocalDate.of(2025, 1, 19),
                        LocalDate.of(2025, 1, 26)
                );
    }
}

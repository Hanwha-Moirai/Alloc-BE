package com.moirai.alloc.report.command.service;

import com.moirai.alloc.common.security.auth.UserPrincipal;
import com.moirai.alloc.report.command.dto.request.CompletedTaskRequest;
import com.moirai.alloc.report.command.dto.request.CreateWeeklyReportRequest;
import com.moirai.alloc.report.command.dto.request.IncompleteTaskRequest;
import com.moirai.alloc.report.command.dto.request.NextWeekTaskRequest;
import com.moirai.alloc.report.command.dto.request.UpdateWeeklyReportRequest;
import com.moirai.alloc.report.command.domain.entity.IssueBlocker;
import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.command.domain.entity.WeeklyTask;
import com.moirai.alloc.report.command.repository.IssueBlockerCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyReportCommandRepository;
import com.moirai.alloc.report.command.repository.WeeklyTaskCommandRepository;
import com.moirai.alloc.report.query.dto.WeeklyReportCreateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
//@Sql(scripts = "/sql/report/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@EnableJpaAuditing
class WeeklyReportCommandServiceTest {

    private static final Long PROJECT_ID = 77001L;
    private static final Long REPORT_ID = 77001L;
    private static final Long PM_USER_ID = 77001L;

    @Autowired
    private WeeklyReportCommandService weeklyReportCommandService;

    @Autowired
    private WeeklyReportCommandRepository weeklyReportCommandRepository;

    @Autowired
    private WeeklyTaskCommandRepository weeklyTaskCommandRepository;

    @Autowired
    private IssueBlockerCommandRepository issueBlockerCommandRepository;

    @Test
    void createWeeklyReport_createsDraft() {
        CreateWeeklyReportRequest request = new CreateWeeklyReportRequest(
                PROJECT_ID,
                LocalDate.of(2025, 1, 6),
                LocalDate.of(2025, 1, 12)
        );
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_77001",
                "pm77001@example.com",
                "PM",
                "PM",
                "pw"
        );

        WeeklyReportCreateResponse response =
                weeklyReportCommandService.createWeeklyReport(PROJECT_ID, request, principal);

        WeeklyReport report = weeklyReportCommandRepository.findByReportIdAndIsDeletedFalse(response.reportId())
                .orElseThrow();
        assertThat(report.getReportStatus()).isEqualTo(WeeklyReport.ReportStatus.DRAFT);
    }

    @Test
    void updateWeeklyReport_savesTasksAndIssueBlockers() {
        UpdateWeeklyReportRequest request = new UpdateWeeklyReportRequest(
                REPORT_ID,
                WeeklyReport.ReportStatus.REVIEWED,
                "변경",
                0.8,
                List.of(new CompletedTaskRequest(77001L)),
                List.of(new IncompleteTaskRequest(77002L, "지연")),
                List.of(new NextWeekTaskRequest(
                        77003L,
                        LocalDate.of(2025, 1, 13),
                        LocalDate.of(2025, 1, 17)
                ))
        );
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_77001",
                "pm77001@example.com",
                "PM",
                "PM",
                "pw"
        );

        weeklyReportCommandService.updateWeeklyReport(PROJECT_ID, request, principal);

        List<WeeklyTask> tasks = weeklyTaskCommandRepository.findAll();
        assertThat(tasks).hasSize(3);
        assertThat(tasks.stream().anyMatch(task -> task.getTaskType() == WeeklyTask.TaskType.NEXT_WEEK)).isTrue();

        List<IssueBlocker> blockers = issueBlockerCommandRepository.findAll();
        assertThat(blockers).hasSize(1);
        assertThat(blockers.get(0).getCauseOfDelay()).isEqualTo("지연");
    }

    @Test
    void deleteWeeklyReport_marksDeleted() {
        UserPrincipal principal = new UserPrincipal(
                PM_USER_ID,
                "pm_77001",
                "pm77001@example.com",
                "PM",
                "PM",
                "pw"
        );

        weeklyReportCommandService.deleteWeeklyReport(PROJECT_ID, REPORT_ID, principal);

        assertThat(weeklyReportCommandRepository.findByReportIdAndIsDeletedFalse(REPORT_ID)).isEmpty();
    }
}

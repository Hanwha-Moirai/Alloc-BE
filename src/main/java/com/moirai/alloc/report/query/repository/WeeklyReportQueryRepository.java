package com.moirai.alloc.report.query.repository;

import com.moirai.alloc.report.command.domain.entity.WeeklyReport;
import com.moirai.alloc.report.query.dto.CompletedTaskResponse;
import com.moirai.alloc.report.query.dto.IncompleteTaskResponse;
import com.moirai.alloc.report.query.dto.NextWeekTaskResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportDetailResponse;
import com.moirai.alloc.report.query.dto.WeeklyReportSearchCondition;
import com.moirai.alloc.report.query.dto.WeeklyReportSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Repository
public class WeeklyReportQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public WeeklyReportQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<WeeklyReportSummaryResponse> findAll(Pageable pageable) {
        String baseSql = "select wr.report_id, wr.project_id, p.name as project_name, u.user_name, " +
                "wr.week_start_date, wr.week_end_date, wr.report_status, wr.task_completion_rate, " +
                "wr.created_at, wr.updated_at " +
                "from weekly_report wr join project p on p.project_id = wr.project_id " +
                "join users u on u.user_id = wr.user_id " +
                "where wr.is_deleted = false";
        String countSql = "select count(1) from weekly_report wr where wr.is_deleted = false";
        String orderSql = " order by wr.created_at desc limit ? offset ?";
        List<WeeklyReportSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                (rs, rowNum) -> {
                    LocalDate weekStartDate = rs.getDate("week_start_date").toLocalDate();
                    return new WeeklyReportSummaryResponse(
                            rs.getLong("report_id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("user_name"),
                            weekStartDate,
                            rs.getDate("week_end_date").toLocalDate(),
                            toWeekLabel(weekStartDate),
                            WeeklyReport.ReportStatus.valueOf(rs.getString("report_status")),
                            rs.getDouble("task_completion_rate"),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            toLocalDateTime(rs.getTimestamp("updated_at"))
                    );
                },
                pageable.getPageSize(),
                pageable.getOffset()
        );
        long total = count(countSql, Collections.emptyList());
        return new PageImpl<>(content, pageable, total);
    }

    public Page<WeeklyReportSummaryResponse> findAllByProjectId(Long projectId, Pageable pageable) {
        if (projectId == null) {
            return findAll(pageable);
        }
        String baseSql = "select wr.report_id, wr.project_id, p.name as project_name, u.user_name, " +
                "wr.week_start_date, wr.week_end_date, wr.report_status, wr.task_completion_rate, " +
                "wr.created_at, wr.updated_at " +
                "from weekly_report wr join project p on p.project_id = wr.project_id " +
                "join users u on u.user_id = wr.user_id " +
                "where wr.is_deleted = false and wr.project_id = ?";
        String countSql = "select count(1) from weekly_report wr where wr.is_deleted = false and wr.project_id = ?";
        String orderSql = " order by wr.created_at desc limit ? offset ?";
        List<Object> params = List.of(projectId, pageable.getPageSize(), pageable.getOffset());
        List<WeeklyReportSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                (rs, rowNum) -> {
                    LocalDate weekStartDate = rs.getDate("week_start_date").toLocalDate();
                    return new WeeklyReportSummaryResponse(
                            rs.getLong("report_id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("user_name"),
                            weekStartDate,
                            rs.getDate("week_end_date").toLocalDate(),
                            toWeekLabel(weekStartDate),
                            WeeklyReport.ReportStatus.valueOf(rs.getString("report_status")),
                            rs.getDouble("task_completion_rate"),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            toLocalDateTime(rs.getTimestamp("updated_at"))
                    );
                },
                params.toArray()
        );
        long total = count(countSql, List.of(projectId));
        return new PageImpl<>(content, pageable, total);
    }

    public Page<WeeklyReportSummaryResponse> search(WeeklyReportSearchCondition condition, Pageable pageable) {
        QueryParts queryParts = buildSearchQuery(condition);
        String orderSql = " order by wr.created_at desc limit ? offset ?";
        List<Object> params = new ArrayList<>(queryParts.params());
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        List<WeeklyReportSummaryResponse> content = jdbcTemplate.query(
                queryParts.sql() + orderSql,
                (rs, rowNum) -> {
                    LocalDate weekStartDate = rs.getDate("week_start_date").toLocalDate();
                    return new WeeklyReportSummaryResponse(
                            rs.getLong("report_id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("user_name"),
                            weekStartDate,
                            rs.getDate("week_end_date").toLocalDate(),
                            toWeekLabel(weekStartDate),
                            WeeklyReport.ReportStatus.valueOf(rs.getString("report_status")),
                            rs.getDouble("task_completion_rate"),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            toLocalDateTime(rs.getTimestamp("updated_at"))
                    );
                },
                params.toArray()
        );
        long total = count(queryParts.countSql(), queryParts.params());
        return new PageImpl<>(content, pageable, total);
    }

    public Page<WeeklyReportSummaryResponse> searchInProjects(List<Long> projectIds,
                                                              WeeklyReportSearchCondition condition,
                                                              Pageable pageable) {
        if (projectIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        QueryParts queryParts = buildSearchQuery(condition);
        String inClause = inClause(projectIds.size());
        String baseSql = queryParts.sql() + " and wr.project_id in " + inClause;
        String countSql = queryParts.countSql() + " and wr.project_id in " + inClause;
        List<Object> params = new ArrayList<>(queryParts.params());
        params.addAll(projectIds);
        String orderSql = " order by wr.created_at desc limit ? offset ?";
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(pageable.getPageSize());
        pageParams.add(pageable.getOffset());
        List<WeeklyReportSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                (rs, rowNum) -> {
                    LocalDate weekStartDate = rs.getDate("week_start_date").toLocalDate();
                    return new WeeklyReportSummaryResponse(
                            rs.getLong("report_id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("user_name"),
                            weekStartDate,
                            rs.getDate("week_end_date").toLocalDate(),
                            toWeekLabel(weekStartDate),
                            WeeklyReport.ReportStatus.valueOf(rs.getString("report_status")),
                            rs.getDouble("task_completion_rate"),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            toLocalDateTime(rs.getTimestamp("updated_at"))
                    );
                },
                pageParams.toArray()
        );
        long total = count(countSql, params);
        return new PageImpl<>(content, pageable, total);
    }

    public Page<WeeklyReportSummaryResponse> findAllByProjectIds(List<Long> projectIds, Pageable pageable) {
        if (projectIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        String inClause = inClause(projectIds.size());
        String baseSql = "select wr.report_id, wr.project_id, p.name as project_name, u.user_name, " +
                "wr.week_start_date, wr.week_end_date, wr.report_status, wr.task_completion_rate, " +
                "wr.created_at, wr.updated_at " +
                "from weekly_report wr join project p on p.project_id = wr.project_id " +
                "join users u on u.user_id = wr.user_id " +
                "where wr.is_deleted = false and wr.project_id in " + inClause;
        String countSql = "select count(1) from weekly_report wr where wr.is_deleted = false and wr.project_id in " +
                inClause;
        String orderSql = " order by wr.created_at desc limit ? offset ?";
        List<Object> params = new ArrayList<>(projectIds);
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        List<WeeklyReportSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                (rs, rowNum) -> {
                    LocalDate weekStartDate = rs.getDate("week_start_date").toLocalDate();
                    return new WeeklyReportSummaryResponse(
                            rs.getLong("report_id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("user_name"),
                            weekStartDate,
                            rs.getDate("week_end_date").toLocalDate(),
                            toWeekLabel(weekStartDate),
                            WeeklyReport.ReportStatus.valueOf(rs.getString("report_status")),
                            rs.getDouble("task_completion_rate"),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            toLocalDateTime(rs.getTimestamp("updated_at"))
                    );
                },
                params.toArray()
        );
        long total = count(countSql, projectIds);
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<WeeklyReportDetailResponse> findDetail(Long reportId) {
        List<WeeklyReportDetailResponse> details = jdbcTemplate.query(
                "select wr.report_id, wr.project_id, p.name as project_name, u.user_name, wr.week_start_date, " +
                        "wr.week_end_date, wr.report_status, wr.task_completion_rate, wr.summary_text, " +
                        "wr.change_of_plan, wr.created_at, wr.updated_at " +
                        "from weekly_report wr join project p on p.project_id = wr.project_id " +
                        "join users u on u.user_id = wr.user_id " +
                        "where wr.is_deleted = false and wr.report_id = ?",
                (rs, rowNum) -> {
                    LocalDate weekStartDate = rs.getDate("week_start_date").toLocalDate();
                    return new WeeklyReportDetailResponse(
                            rs.getLong("report_id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("user_name"),
                            weekStartDate,
                            rs.getDate("week_end_date").toLocalDate(),
                            toWeekLabel(weekStartDate),
                            WeeklyReport.ReportStatus.valueOf(rs.getString("report_status")),
                            rs.getDouble("task_completion_rate"),
                            rs.getString("summary_text"),
                            rs.getString("change_of_plan"),
                            findCompletedTasks(reportId),
                            findIncompleteTasks(reportId),
                            findNextWeekTasks(reportId),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            toLocalDateTime(rs.getTimestamp("updated_at"))
                    );
                },
                reportId
        );
        if (details.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(details.get(0));
    }

    public List<LocalDate> findWeekStartDates(Long projectId,
                                              Long userId,
                                              LocalDate startDate,
                                              LocalDate endDate) {
        return jdbcTemplate.query(
                "select distinct week_start_date " +
                        "from weekly_report " +
                        "where is_deleted = false and project_id = ? and user_id = ? " +
                        "and week_start_date >= ? and week_start_date <= ? " +
                        "order by week_start_date",
                (rs, rowNum) -> rs.getDate("week_start_date").toLocalDate(),
                projectId,
                userId,
                startDate,
                endDate
        );
    }

    private List<CompletedTaskResponse> findCompletedTasks(Long reportId) {
        return jdbcTemplate.query(
                "select t.task_id, t.task_name, u.user_name, t.task_category, t.updated_at " +
                        "from weekly_tasks wt " +
                        "join task t on t.task_id = wt.task_id " +
                        "join users u on u.user_id = t.user_id " +
                        "where wt.report_id = ? and wt.task_type = 'COMPLETED' and wt.is_completed = true",
                (rs, rowNum) -> new CompletedTaskResponse(
                        rs.getLong("task_id"),
                        rs.getString("task_name"),
                        rs.getString("user_name"),
                        com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory.valueOf(
                                rs.getString("task_category")
                        ),
                        toLocalDateTime(rs.getTimestamp("updated_at"))
                ),
                reportId
        );
    }

    private List<IncompleteTaskResponse> findIncompleteTasks(Long reportId) {
        return jdbcTemplate.query(
                "select t.task_id, t.task_name, u.user_name, t.task_category, ib.delayed_dates, ib.cause_of_delay " +
                "from weekly_tasks wt " +
                "join task t on t.task_id = wt.task_id " +
                "join users u on u.user_id = t.user_id " +
                "left join issue_blockers ib on ib.weekly_tasks_id = wt.weekly_tasks_id " +
                "where wt.report_id = ? and wt.task_type = 'INCOMPLETE'",
                (rs, rowNum) -> new IncompleteTaskResponse(
                        rs.getLong("task_id"),
                        rs.getString("task_name"),
                        rs.getString("user_name"),
                        com.moirai.alloc.gantt.command.domain.entity.Task.TaskCategory.valueOf(
                                rs.getString("task_category")
                        ),
                        rs.getObject("delayed_dates", Integer.class),
                        rs.getString("cause_of_delay")
                ),
                reportId
        );
    }

    private List<NextWeekTaskResponse> findNextWeekTasks(Long reportId) {
        return jdbcTemplate.query(
                "select t.task_id, t.task_name, u.user_name, " +
                        "coalesce(wt.planned_start_date, t.start_date) as planned_start_date, " +
                        "coalesce(wt.planned_end_date, t.end_date) as planned_end_date " +
                        "from weekly_tasks wt " +
                        "join task t on t.task_id = wt.task_id " +
                        "join users u on u.user_id = t.user_id " +
                        "where wt.report_id = ? and wt.task_type = 'NEXT_WEEK'",
                (rs, rowNum) -> new NextWeekTaskResponse(
                        rs.getLong("task_id"),
                        rs.getString("task_name"),
                        rs.getString("user_name"),
                        rs.getDate("planned_start_date").toLocalDate(),
                        rs.getDate("planned_end_date").toLocalDate()
                ),
                reportId
        );
    }

    private QueryParts buildSearchQuery(WeeklyReportSearchCondition condition) {
        String baseSql = "from weekly_report wr join project p on p.project_id = wr.project_id " +
                "join users u on u.user_id = wr.user_id " +
                "where wr.is_deleted = false";
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (condition.projectId() != null) {
            where.append(" and wr.project_id = ?");
            params.add(condition.projectId());
        }
        if (condition.projectName() != null && !condition.projectName().isBlank()) {
            where.append(" and p.name like ?");
            params.add("%" + condition.projectName().trim() + "%");
        }
        if (condition.reportStatus() != null) {
            where.append(" and wr.report_status = ?");
            params.add(condition.reportStatus().name());
        }
        if (condition.weekStartFrom() != null) {
            where.append(" and wr.week_start_date >= ?");
            params.add(condition.weekStartFrom());
        }
        if (condition.weekStartTo() != null) {
            where.append(" and wr.week_start_date <= ?");
            params.add(condition.weekStartTo());
        }
        String selectSql = "select wr.report_id, wr.project_id, p.name as project_name, u.user_name, " +
                "wr.week_start_date, wr.week_end_date, wr.report_status, wr.task_completion_rate, " +
                "wr.created_at, wr.updated_at " +
                baseSql + where;
        String countSql = "select count(1) " + baseSql + where;
        return new QueryParts(selectSql, countSql, params);
    }

    private long count(String countSql, List<?> params) {
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
        return Objects.requireNonNullElse(total, 0);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String toWeekLabel(LocalDate weekStartDate) {
        if (weekStartDate == null) {
            return null;
        }
        int week = weekStartDate.get(WeekFields.of(Locale.KOREA).weekOfMonth());
        return String.format("%d년 %d월 %d주차", weekStartDate.getYear(), weekStartDate.getMonthValue(), week);
    }

    private String inClause(int size) {
        StringBuilder builder = new StringBuilder("(");
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append("?");
        }
        builder.append(")");
        return builder.toString();
    }

    private record QueryParts(String sql, String countSql, List<Object> params) {
    }
}

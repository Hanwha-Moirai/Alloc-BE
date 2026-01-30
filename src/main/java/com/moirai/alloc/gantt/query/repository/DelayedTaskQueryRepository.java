package com.moirai.alloc.gantt.query.repository;

import com.moirai.alloc.gantt.query.dto.request.DelayedTaskSearchRequest;
import com.moirai.alloc.gantt.query.dto.response.DelayedTaskResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DelayedTaskQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DelayedTaskQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DelayedTaskResponse> findDelayedTasks(DelayedTaskSearchRequest condition) {
        QueryParts queryParts = buildQuery(condition);
        return jdbcTemplate.query(
                queryParts.sql(),
                (rs, rowNum) -> new DelayedTaskResponse(
                        rs.getString("task_name"),
                        rs.getString("project_name"),
                        rs.getString("assignee_name"),
                        rs.getObject("delayed_days", Integer.class)
                ),
                queryParts.params().toArray()
        );
    }

    private QueryParts buildQuery(DelayedTaskSearchRequest condition) {
        String baseSql = "from task t " +
                "join milestone m on m.milestone_id = t.milestone_id " +
                "join project p on p.project_id = m.project_id " +
                "join users u on u.user_id = t.user_id " +
                "where t.is_deleted = false " +
                "and t.is_completed = false " +
                "and m.is_deleted = false " +
                "and t.end_date < curdate()";
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (condition.taskName() != null && !condition.taskName().isBlank()) {
            where.append(" and t.task_name like ?");
            params.add("%" + condition.taskName().trim() + "%");
        }
        if (condition.projectName() != null && !condition.projectName().isBlank()) {
            where.append(" and p.name like ?");
            params.add("%" + condition.projectName().trim() + "%");
        }
        if (condition.assigneeName() != null && !condition.assigneeName().isBlank()) {
            where.append(" and u.user_name like ?");
            params.add("%" + condition.assigneeName().trim() + "%");
        }
        if (condition.delayedDays() != null) {
            where.append(" and datediff(curdate(), t.end_date) >= ?");
            params.add(condition.delayedDays());
        }

        String selectSql = "select t.task_name as task_name, " +
                "p.name as project_name, " +
                "u.user_name as assignee_name, " +
                "datediff(curdate(), t.end_date) as delayed_days " +
                baseSql + where +
                " order by delayed_days desc, t.task_id asc";
        return new QueryParts(selectSql, params);
    }

    private record QueryParts(String sql, List<Object> params) {
    }
}

package com.moirai.alloc.meeting.query.repository;

import com.moirai.alloc.meeting.query.dto.response.AgendaResponse;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordDetailResponse;
import com.moirai.alloc.meeting.query.dto.MeetingRecordSearchCondition;
import com.moirai.alloc.meeting.query.dto.response.MeetingRecordSummaryResponse;
import com.moirai.alloc.meeting.query.dto.response.ParticipantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MeetingRecordQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingRecordQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Page<MeetingRecordSummaryResponse> findAll(Pageable pageable) {
        String baseSql = "select mr.meeting_id, mr.project_id, p.name as project_name, mr.created_by, mr.progress, " +
                "mr.meeting_date, mr.meeting_time, mr.created_at, mr.updated_at " +
                "from meeting_record mr join project p on p.project_id = mr.project_id " +
                "where mr.is_deleted = false";
        String countSql = "select count(1) from meeting_record where is_deleted = false";
        String orderSql = " order by created_at desc limit ? offset ?";
        List<MeetingRecordSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                summaryRowMapper(),
                pageable.getPageSize(),
                pageable.getOffset()
        );
        long total = count(countSql, Collections.emptyList());
        return new PageImpl<>(content, pageable, total);
    }

    public Page<MeetingRecordSummaryResponse> findAllByProjectId(Long projectId, Pageable pageable) {
        String baseSql = "select mr.meeting_id, mr.project_id, p.name as project_name, mr.created_by, mr.progress, " +
                "mr.meeting_date, mr.meeting_time, mr.created_at, mr.updated_at " +
                "from meeting_record mr join project p on p.project_id = mr.project_id " +
                "where mr.is_deleted = false and mr.project_id = ?";
        String countSql = "select count(1) from meeting_record where is_deleted = false and project_id = ?";
        String orderSql = " order by created_at desc limit ? offset ?";
        List<Object> params = new ArrayList<>();
        params.add(projectId);
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        List<MeetingRecordSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                summaryRowMapper(),
                params.toArray()
        );
        long total = count(countSql, List.of(projectId));
        return new PageImpl<>(content, pageable, total);
    }

    public Page<MeetingRecordSummaryResponse> search(MeetingRecordSearchCondition condition, Pageable pageable) {
        QueryParts queryParts = buildSearchQuery(condition);
        String orderSql = " order by created_at desc limit ? offset ?";
        List<Object> params = new ArrayList<>(queryParts.params());
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        List<MeetingRecordSummaryResponse> content = jdbcTemplate.query(
                queryParts.sql() + orderSql,
                summaryRowMapper(),
                params.toArray()
        );
        long total = count(queryParts.countSql(), queryParts.params());
        return new PageImpl<>(content, pageable, total);
    }

    public Page<MeetingRecordSummaryResponse> findAllByProjectIds(List<Long> projectIds, Pageable pageable) {
        if (projectIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        String inClause = inClause(projectIds.size());
        String baseSql = "select mr.meeting_id, mr.project_id, p.name as project_name, mr.created_by, mr.progress, " +
                "mr.meeting_date, mr.meeting_time, mr.created_at, mr.updated_at " +
                "from meeting_record mr join project p on p.project_id = mr.project_id " +
                "where mr.is_deleted = false and mr.project_id in " + inClause;
        String countSql = "select count(1) from meeting_record where is_deleted = false and project_id in " + inClause;
        String orderSql = " order by created_at desc limit ? offset ?";
        List<Object> params = new ArrayList<>(projectIds);
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        List<MeetingRecordSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                summaryRowMapper(),
                params.toArray()
        );
        long total = count(countSql, projectIds);
        return new PageImpl<>(content, pageable, total);
    }

    public Page<MeetingRecordSummaryResponse> searchInProjects(List<Long> projectIds,
                                                               MeetingRecordSearchCondition condition,
                                                               Pageable pageable) {
        if (projectIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        QueryParts queryParts = buildSearchQuery(condition);
        String inClause = inClause(projectIds.size());
        String baseSql = queryParts.sql() + " and mr.project_id in " + inClause;
        String countSql = queryParts.countSql() + " and mr.project_id in " + inClause;
        List<Object> params = new ArrayList<>(queryParts.params());
        params.addAll(projectIds);
        String orderSql = " order by created_at desc limit ? offset ?";
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(pageable.getPageSize());
        pageParams.add(pageable.getOffset());
        List<MeetingRecordSummaryResponse> content = jdbcTemplate.query(
                baseSql + orderSql,
                summaryRowMapper(),
                pageParams.toArray()
        );
        long total = count(countSql, params);
        return new PageImpl<>(content, pageable, total);
    }

    public Optional<MeetingRecordDetailResponse> findDetail(Long meetingId) {
        List<MeetingRecordSummaryResponse> records = jdbcTemplate.query(
                "select mr.meeting_id, mr.project_id, p.name as project_name, mr.created_by, mr.progress, " +
                        "mr.meeting_date, mr.meeting_time, mr.created_at, mr.updated_at " +
                        "from meeting_record mr join project p on p.project_id = mr.project_id " +
                        "where mr.meeting_id = ? and mr.is_deleted = false",
                summaryRowMapper(),
                meetingId
        );
        if (records.isEmpty()) {
            return Optional.empty();
        }
        MeetingRecordSummaryResponse summary = records.get(0);
        List<AgendaResponse> agendas = findAgendas(meetingId);
        List<ParticipantResponse> participants = findParticipants(meetingId);
        return Optional.of(new MeetingRecordDetailResponse(
                summary.meetingId(),
                summary.projectId(),
                summary.createdBy(),
                summary.progress(),
                summary.meetingDate(),
                summary.meetingTime(),
                summary.createdAt(),
                summary.updatedAt(),
                agendas,
                participants
        ));
    }

    private List<AgendaResponse> findAgendas(Long meetingId) {
        return jdbcTemplate.query(
                "select agenda_id, discussion_title, discussion_content, discussion_result, agenda_type " +
                        "from agenda where meeting_id = ?",
                (rs, rowNum) -> new AgendaResponse(
                        rs.getLong("agenda_id"),
                        rs.getString("discussion_title"),
                        rs.getString("discussion_content"),
                        rs.getString("discussion_result"),
                        rs.getString("agenda_type")
                ),
                meetingId
        );
    }

    private List<ParticipantResponse> findParticipants(Long meetingId) {
        return jdbcTemplate.query(
                "select user_id, is_host from participants where meeting_id = ?",
                (rs, rowNum) -> new ParticipantResponse(
                        rs.getLong("user_id"),
                        rs.getBoolean("is_host")
                ),
                meetingId
        );
    }

    private QueryParts buildSearchQuery(MeetingRecordSearchCondition condition) {
        String baseSql = "from meeting_record mr join project p on p.project_id = mr.project_id " +
                "where mr.is_deleted = false";
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (condition.from() != null) {
            where.append(" and mr.meeting_date >= ?");
            params.add(startOfDay(condition.from()));
        }
        if (condition.to() != null) {
            where.append(" and mr.meeting_date <= ?");
            params.add(endOfDay(condition.to()));
        }
        if (condition.projectName() != null && !condition.projectName().isBlank()) {
            where.append(" and p.name like ?");
            params.add("%" + condition.projectName().trim() + "%");
        }
        String selectSql = "select mr.meeting_id, mr.project_id, p.name as project_name, mr.created_by, mr.progress, " +
                "mr.meeting_date, mr.meeting_time, mr.created_at, mr.updated_at " + baseSql + where;
        String countSql = "select count(1) " + baseSql + where;
        return new QueryParts(selectSql, countSql, params);
    }

    private RowMapper<MeetingRecordSummaryResponse> summaryRowMapper() {
        return (rs, rowNum) -> new MeetingRecordSummaryResponse(
                rs.getLong("meeting_id"),
                rs.getLong("project_id"),
                rs.getString("project_name"),
                rs.getString("created_by"),
                getDouble(rs.getObject("progress")),
                toLocalDateTime(rs.getTimestamp("meeting_date")),
                toLocalDateTime(rs.getTimestamp("meeting_time")),
                toLocalDateTime(rs.getTimestamp("created_at")),
                toLocalDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private Double getDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private long count(String countSql, List<?> params) {
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
        return Objects.requireNonNullElse(total, 0);
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

    private LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }

    private record QueryParts(String sql, String countSql, List<Object> params) {
    }
}

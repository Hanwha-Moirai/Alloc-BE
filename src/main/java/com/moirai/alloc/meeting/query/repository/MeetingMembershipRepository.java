package com.moirai.alloc.meeting.query.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeetingMembershipRepository {

    private final JdbcTemplate jdbcTemplate;

    public MeetingMembershipRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsMembership(Long projectId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from squad_assignment where project_id = ? and user_id = ?",
                Integer.class,
                projectId,
                userId
        );
        return count != null && count > 0;
    }

    public List<Long> findProjectIdsByUserId(Long userId) {
        return jdbcTemplate.query(
                "select distinct project_id from squad_assignment where user_id = ?",
                (rs, rowNum) -> rs.getLong("project_id"),
                userId
        );
    }
}

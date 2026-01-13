INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (88001, 'pm_88001', 'pw', 'PM User', 'pm88001@example.com', '01000000001', 'ACTIVE', 'PM'),
    (88002, 'member_88002', 'pw', 'Member User', 'member88002@example.com', '01000000002', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE login_id = VALUES(login_id);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (88001, 'Meeting Project', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW'),
    (88002, 'Other Project', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO squad_assignment (assignment_id, project_id, user_id, proposed_at, assignment_status, final_decision, decided_at)
VALUES
    (88001, 88001, 88001, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL),
    (88002, 88001, 88002, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL)
ON DUPLICATE KEY UPDATE assignment_status = VALUES(assignment_status);

INSERT INTO meeting_record (meeting_id, project_id, created_by, progress, meeting_date, meeting_time, is_deleted, created_at, updated_at)
VALUES
    (88001, 88001, 'pm_88001', 10.0, '2025-01-05 10:00:00', '2025-01-05 10:00:00', 0, '2025-01-05 10:00:00', '2025-01-05 10:00:00'),
    (88002, 88002, 'pm_88001', 20.0, '2025-01-06 10:00:00', '2025-01-06 10:00:00', 0, '2025-01-06 10:00:00', '2025-01-06 10:00:00')
ON DUPLICATE KEY UPDATE
    project_id = VALUES(project_id),
    created_by = VALUES(created_by),
    progress = VALUES(progress),
    meeting_date = VALUES(meeting_date),
    meeting_time = VALUES(meeting_time),
    is_deleted = VALUES(is_deleted);

INSERT INTO agenda (agenda_id, meeting_id, discussion_title, discussion_content, discussion_result, agenda_type)
VALUES
    (88001, 88001, 'API 논의', '검색키워드 포함', '결정', 'TYPE1')
ON DUPLICATE KEY UPDATE discussion_title = VALUES(discussion_title);

INSERT INTO participants (participants_id, meeting_id, user_id, is_host)
VALUES
    (88001, 88001, 88002, 0)
ON DUPLICATE KEY UPDATE is_host = VALUES(is_host);

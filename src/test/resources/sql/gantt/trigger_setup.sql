INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (99101, 'trigger_user_99101', 'pw', 'Trigger User', 'trigger99101@example.com', '01000000101', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE login_id = VALUES(login_id);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (99100, 'Trigger Test Project', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, is_completed, created_at, updated_at)
VALUES
    (99101, 99100, 'Trigger M1', '2025-01-01', '2025-01-10', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99102, 99100, 'Trigger M2', '2025-01-11', '2025-01-20', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99103, 99100, 'Trigger M3', '2025-01-21', '2025-01-30', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99104, 99100, 'Trigger M4', '2025-02-01', '2025-02-10', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00')
ON DUPLICATE KEY UPDATE milestone_name = VALUES(milestone_name);

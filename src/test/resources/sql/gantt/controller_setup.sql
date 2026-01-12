INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (99100, 'pm_99100', 'pw', 'PM User', 'pm99100@example.com', '01000000001', 'ACTIVE', 'PM'),
    (99101, 'user_99101', 'pw', 'User One', 'user99101@example.com', '01000000002', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE login_id = VALUES(login_id);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (99100, 'Gantt Controller Project', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO squad_assignment (assignment_id, project_id, user_id, proposed_at, assignment_status, final_decision, decided_at)
VALUES
    (99100, 99100, 99100, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL),
    (99101, 99100, 99101, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL)
ON DUPLICATE KEY UPDATE assignment_status = VALUES(assignment_status);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, created_at, updated_at)
VALUES
    (99100, 99100, 'M1', '2025-01-01', '2025-01-15', 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99101, 99100, 'M2', '2025-01-16', '2025-01-31', 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00')
ON DUPLICATE KEY UPDATE milestone_name = VALUES(milestone_name);

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at)
VALUES
    (99100, 99100, 99101, 'DEVELOPMENT', 'Seed Task 1', 'desc', 'TODO', '2025-01-02', '2025-01-05', 0, 0, '2025-01-02 00:00:00', '2025-01-02 00:00:00')
ON DUPLICATE KEY UPDATE task_name = VALUES(task_name);

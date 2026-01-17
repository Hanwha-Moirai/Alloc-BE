INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (99100, 'pm_99100', 'pw', 'PM User', 'pm99100@example.com', '01000000001', 'ACTIVE', 'PM'),
    (99101, 'user_99101', 'pw', 'User One', 'user99101@example.com', '01000000002', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE
    login_id = VALUES(login_id),
    user_name = VALUES(user_name),
    auth = VALUES(auth);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (99100, 'Gantt Controller Project', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date);

INSERT INTO squad_assignment (assignment_id, project_id, user_id, proposed_at, assignment_status, final_decision, decided_at)
VALUES
    (99100, 99100, 99100, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL),
    (99101, 99100, 99101, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL)
ON DUPLICATE KEY UPDATE
    project_id = VALUES(project_id),
    user_id = VALUES(user_id),
    assignment_status = VALUES(assignment_status),
    final_decision = VALUES(final_decision);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, is_completed, created_at, updated_at)
VALUES
    (99100, 99100, 'M1', '2025-01-01', '2025-01-15', 0, 0, 1, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99101, 99100, 'M2', '2025-01-16', '2025-01-31', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99102, 99100, 'M3', '2025-02-01', '2025-02-15', 0, 1, 1, '2025-01-01 00:00:00', '2025-01-01 00:00:00')
ON DUPLICATE KEY UPDATE
    project_id = VALUES(project_id),
    milestone_name = VALUES(milestone_name),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    is_deleted = VALUES(is_deleted);

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at)
VALUES
    (99100, 99100, 99101, 'DEVELOPMENT', 'Seed Task 1', 'desc', 'TODO', '2025-01-02', '2025-01-05', 0, 0, '2025-01-02 00:00:00', '2025-01-02 00:00:00')
ON DUPLICATE KEY UPDATE
    milestone_id = VALUES(milestone_id),
    user_id = VALUES(user_id),
    task_category = VALUES(task_category),
    task_name = VALUES(task_name),
    task_description = VALUES(task_description),
    task_status = VALUES(task_status),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    is_completed = VALUES(is_completed),
    is_deleted = VALUES(is_deleted);

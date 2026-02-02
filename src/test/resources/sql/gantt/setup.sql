DELETE FROM employee_skill WHERE user_id IN (99001, 99002);
DELETE FROM employee WHERE user_id IN (99001, 99002);

INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (99001, 'test_pm_99001', 'pw', 'PM User', 'pm99001@example.com', '01000000001', 'ACTIVE', 'PM'),
    (99002, 'test_user_99002', 'pw', 'User Two', 'user99002@example.com', '01000000002', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE login_id = VALUES(login_id);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (99001, 'Test Project 99001', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO squad_assignment (assignment_id, project_id, user_id, proposed_at, assignment_status, final_decision, decided_at)
VALUES
    (99001, 99001, 99001, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL),
    (99002, 99001, 99002, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL)
ON DUPLICATE KEY UPDATE assignment_status = VALUES(assignment_status);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, is_completed, created_at, updated_at)
VALUES
    (99001, 99001, 'M1', '2025-01-01', '2025-01-15', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99002, 99001, 'M2', '2025-01-16', '2025-01-31', 0, 0, 1, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99003, 99001, 'M3', '2025-02-01', '2025-02-15', 0, 1, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00')
ON DUPLICATE KEY UPDATE milestone_name = VALUES(milestone_name);

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at)
VALUES
    (99001, 99001, 99002, 'DEVELOPMENT', 'Seed Task 1', 'desc', 'TODO', '2025-01-02', '2025-01-05', 0, 0, '2025-01-02 00:00:00', '2025-01-02 00:00:00'),
    (99002, 99002, 99002, 'TESTING', 'Done Task', 'desc', 'DONE', '2025-01-03', '2025-01-04', 1, 0, '2025-01-03 00:00:00', '2025-01-05 00:00:00')
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
    is_deleted = VALUES(is_deleted),
    updated_at = VALUES(updated_at);

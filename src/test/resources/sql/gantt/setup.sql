INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (99001, 'test_pm_99001', 'pw', 'PM User', 'pm99001@example.com', '01000000001', 'ACTIVE', 'PM'),
    (99002, 'test_user_99002', 'pw', 'User Two', 'user99002@example.com', '01000000002', 'ACTIVE', 'USER');

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (99001, 'Test Project 99001', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW');

INSERT INTO squad_assignment (assignment_id, project_id, user_id, proposed_at, assignment_status, final_decision, decided_at)
VALUES
    (99001, 99001, 99001, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL),
    (99002, 99001, 99002, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, created_at, updated_at)
VALUES
    (99001, 99001, 'M1', '2025-01-01', '2025-01-15', 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99002, 99001, 'M2', '2025-01-16', '2025-01-31', 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00');

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at)
VALUES
    (99001, 99001, 99002, 'DEVELOPMENT', 'Seed Task 1', 'desc', 'TODO', '2025-01-02', '2025-01-05', 0, 0, '2025-01-02 00:00:00', '2025-01-02 00:00:00'),
    (99002, 99002, 99002, 'TESTING', 'Done Task', 'desc', 'DONE', '2025-01-03', '2025-01-04', 1, 0, '2025-01-03 00:00:00', '2025-01-05 00:00:00');

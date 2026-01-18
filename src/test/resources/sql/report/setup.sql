INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (77001, 'pm_77001', 'pw', 'PM User', 'pm77001@example.com', '01000000001', 'ACTIVE', 'PM'),
    (77002, 'member_77002', 'pw', 'Member User', 'member77002@example.com', '01000000002', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE login_id = VALUES(login_id);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (77001, 'Report Project', '2025-01-01', '2025-02-01', 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO squad_assignment (assignment_id, project_id, user_id, proposed_at, assignment_status, final_decision, decided_at)
VALUES
    (77001, 77001, 77001, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL),
    (77002, 77001, 77002, '2025-01-01 00:00:00', 'REQUESTED', 'ASSIGNED', NULL)
ON DUPLICATE KEY UPDATE assignment_status = VALUES(assignment_status);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, is_completed, created_at, updated_at)
VALUES
    (77001, 77001, 'M1', '2025-01-01', '2025-01-15', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (77002, 77001, 'M2', '2025-01-16', '2025-01-31', 0, 0, 0, '2025-01-01 00:00:00', '2025-01-01 00:00:00')
ON DUPLICATE KEY UPDATE milestone_name = VALUES(milestone_name);

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at)
VALUES
    (77001, 77001, 77002, 'DEVELOPMENT', 'Done Task', 'desc', 'DONE', '2025-01-02', '2025-01-05', 1, 0, '2025-01-02 00:00:00', '2025-01-08 00:00:00'),
    (77002, 77001, 77002, 'TESTING', 'In Progress Task', 'desc', 'INPROGRESS', '2025-01-03', '2025-01-06', 0, 0, '2025-01-03 00:00:00', '2025-01-03 00:00:00'),
    (77003, 77002, 77002, 'DEVELOPMENT', 'Next Week Task', 'desc', 'TODO', '2025-01-13', '2025-01-17', 0, 0, '2025-01-04 00:00:00', '2025-01-04 00:00:00')
ON DUPLICATE KEY UPDATE task_name = VALUES(task_name);

INSERT INTO weekly_report (report_id, user_id, project_id, week_start_date, week_end_date, report_status, change_of_plan, task_completion_rate, summary_text, is_deleted, created_at, updated_at)
VALUES
    (77001, 77001, 77001, '2025-01-06', '2025-01-12', 'DRAFT', '변경', 0.5, 'summary', 0, '2025-01-12 00:00:00', '2025-01-12 00:00:00')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    project_id = VALUES(project_id),
    week_start_date = VALUES(week_start_date),
    week_end_date = VALUES(week_end_date),
    report_status = VALUES(report_status),
    change_of_plan = VALUES(change_of_plan),
    task_completion_rate = VALUES(task_completion_rate),
    summary_text = VALUES(summary_text),
    is_deleted = VALUES(is_deleted);

INSERT INTO weekly_tasks (weekly_tasks_id, report_id, task_id, task_type, planned_start_date, planned_end_date)
VALUES
    (77001, 77001, 77001, 'COMPLETED', NULL, NULL),
    (77002, 77001, 77002, 'INCOMPLETE', NULL, NULL),
    (77003, 77001, 77003, 'NEXT_WEEK', '2025-01-13', '2025-01-17')
ON DUPLICATE KEY UPDATE task_type = VALUES(task_type);

INSERT INTO issue_blockers (issue_blockers_id, weekly_tasks_id, cause_of_delay, dependency_summary, delayed_dates)
VALUES
    (77001, 77002, 'Blocked', NULL, NULL)
ON DUPLICATE KEY UPDATE cause_of_delay = VALUES(cause_of_delay);

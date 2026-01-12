DELETE FROM issue_blockers WHERE issue_blockers_id IN (77001);
DELETE FROM weekly_tasks WHERE weekly_tasks_id IN (77001, 77002, 77003);
DELETE FROM weekly_report WHERE report_id IN (77001);
DELETE FROM task_update_log WHERE task_id IN (77001, 77002, 77003);
DELETE FROM task WHERE task_id IN (77001, 77002, 77003);
DELETE FROM milestone WHERE milestone_id IN (77001, 77002);
DELETE FROM squad_assignment WHERE assignment_id IN (77001, 77002);
DELETE FROM project WHERE project_id IN (77001);
DELETE FROM users WHERE user_id IN (77001, 77002);

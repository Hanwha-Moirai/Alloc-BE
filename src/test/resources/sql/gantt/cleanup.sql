DELETE FROM task_update_log WHERE task_id IN (99001, 99002);
DELETE FROM milestone_update_log WHERE milestone_id IN (99001, 99002);
DELETE FROM task WHERE task_id IN (99001, 99002);
DELETE FROM milestone WHERE milestone_id IN (99001, 99002);
DELETE FROM squad_assignment WHERE assignment_id IN (99001, 99002);
DELETE FROM project WHERE project_id = 99001;
DELETE FROM users WHERE user_id IN (99001, 99002);

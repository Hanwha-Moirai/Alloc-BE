DELETE FROM task_update_log WHERE task_id IN (99100);
DELETE FROM task WHERE task_id IN (99100);
DELETE FROM milestone WHERE milestone_id IN (99100, 99101);
DELETE FROM squad_assignment WHERE assignment_id IN (99100, 99101);
DELETE FROM project WHERE project_id IN (99100);
DELETE FROM users WHERE user_id IN (99100, 99101);

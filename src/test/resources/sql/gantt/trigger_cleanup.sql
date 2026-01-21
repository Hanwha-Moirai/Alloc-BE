DELETE FROM task_update_log
WHERE task_id IN (
    SELECT task_id
    FROM task
    WHERE milestone_id IN (
        SELECT milestone_id
        FROM milestone
        WHERE project_id = 99100
    )
);
DELETE FROM task
WHERE milestone_id IN (
    SELECT milestone_id
    FROM milestone
    WHERE project_id = 99100
);
DELETE FROM milestone_update_log
WHERE milestone_id IN (
    SELECT milestone_id
    FROM milestone
    WHERE project_id = 99100
);
DELETE FROM milestone WHERE project_id = 99100;
DELETE FROM project WHERE project_id = 99100;
DELETE FROM users WHERE user_id IN (99101);

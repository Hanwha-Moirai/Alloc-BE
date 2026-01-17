DELETE FROM issue_blockers
WHERE weekly_tasks_id IN (
    SELECT weekly_tasks_id
    FROM weekly_tasks
    WHERE report_id IN (
        SELECT report_id
        FROM weekly_report
        WHERE project_id IN (77001)
    )
);
DELETE FROM weekly_tasks
WHERE report_id IN (
    SELECT report_id
    FROM weekly_report
    WHERE project_id IN (77001)
);
DELETE FROM weekly_report WHERE project_id IN (77001);
DELETE FROM task
WHERE milestone_id IN (
    SELECT milestone_id
    FROM milestone
    WHERE project_id IN (77001)
);
DELETE FROM milestone WHERE project_id IN (77001);
DELETE FROM squad_assignment WHERE project_id IN (77001);
DELETE FROM project WHERE project_id IN (77001);
DELETE FROM users WHERE user_id IN (77001, 77002);

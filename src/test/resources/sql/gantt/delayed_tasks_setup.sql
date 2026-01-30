INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (99111, 'delay_user_a', 'pw', 'Delay User A', 'delayA@example.com', '01000000111', 'ACTIVE', 'USER'),
    (99112, 'delay_user_b', 'pw', 'Delay User B', 'delayB@example.com', '01000000112', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE login_id = VALUES(login_id);

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type)
VALUES
    (99101, 'Delay Project A', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', 'NEW'),
    (99102, 'Delay Project B', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', 'NEW')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, is_completed, created_at, updated_at)
VALUES
    (99201, 99101, 'Delay M1', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 0, 0, 0, NOW(), NOW()),
    (99202, 99102, 'Delay M2', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 0, 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE milestone_name = VALUES(milestone_name);

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at)
VALUES
    (99301, 99201, 99111, 'DEVELOPMENT', 'Delay Task Alpha', 'desc', 'INPROGRESS',
     DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 0, 0, NOW(), NOW()),
    (99302, 99202, 99112, 'TESTING', 'Delay Task Beta', 'desc', 'INPROGRESS',
     DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 3 DAY), 0, 0, NOW(), NOW()),
    (99303, 99201, 99111, 'DEVELOPMENT', 'Future Task', 'desc', 'TODO',
     DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 0, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE task_name = VALUES(task_name);

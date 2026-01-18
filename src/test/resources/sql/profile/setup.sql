INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (77001, 'kmj', 'pass', '김명진', 'kmj@alloc.co.kr', '010-1234-5678', 'ACTIVE', 'USER'),
    (77002, 'nostack', 'pass', '기술스택없음', 'nostack@alloc.co.kr', '010-0000-0000', 'ACTIVE', 'USER'),
    (77003, 'nohistory', 'pass', '히스토리없음', 'nohistory@alloc.co.kr', '010-1111-1111', 'ACTIVE', 'USER')
ON DUPLICATE KEY UPDATE
    login_id = VALUES(login_id),
    user_name = VALUES(user_name),
    email = VALUES(email),
    phone = VALUES(phone),
    status = VALUES(status),
    auth = VALUES(auth);

INSERT INTO department (dept_id, dept_name, manager_id, created_at, updated_at)
VALUES (1, 'IT', 77001, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    dept_name = VALUES(dept_name),
    manager_id = VALUES(manager_id),
    updated_at = VALUES(updated_at);

INSERT INTO job_standard (job_id, job_name, created_at, updated_at)
VALUES (1, 'BackendDeveloper', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    updated_at = VALUES(updated_at);

INSERT INTO title_standard (title_standard_id, title_name, created_at, updated_at)
VALUES (1, '사원', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    title_name = VALUES(title_name),
    updated_at = VALUES(updated_at);

INSERT INTO employee (user_id, job_id, dept_id, employee_type, title_standard_id, hiring_date)
VALUES
    (77001, 1, 1, 'FULL_TIME', 1, DATE '2022-03-01'),
    (77002, 1, 1, 'FULL_TIME', 1, DATE '2023-01-01'),
    (77003, 1, 1, 'FULL_TIME', 1, DATE '2024-01-01')
ON DUPLICATE KEY UPDATE
    job_id = VALUES(job_id),
    dept_id = VALUES(dept_id),
    employee_type = VALUES(employee_type),
    title_standard_id = VALUES(title_standard_id),
    hiring_date = VALUES(hiring_date);

INSERT INTO tech_standard (tech_id, tech_name, created_at, updated_at)
VALUES
    (1, 'Java', NOW(), NOW()),
    (2, 'Spring', NOW(), NOW()),
    (3, 'JPA', NOW(), NOW()),
    (4, 'Python', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    tech_name = VALUES(tech_name),
    updated_at = VALUES(updated_at);

INSERT INTO employee_skill (employee_tech_id, user_id, tech_id, proficiency, created_at, updated_at)
VALUES
    (1001, 77001, 1, 'LV3', NOW(), NOW()),
    (1002, 77001, 2, 'LV2', NOW(), NOW()),
    (1003, 77001, 3, 'LV1', NOW(), NOW()),
    (1004, 77001, 4, 'LV2', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    proficiency = VALUES(proficiency),
    updated_at = VALUES(updated_at);

INSERT INTO project (project_id, name, start_date, end_date, project_status)
VALUES
    (101, 'Project A', DATE '2023-01-01', DATE '2023-12-31', 'ACTIVE'),
    (102, 'Project B', DATE '2022-01-01', DATE '2022-12-31', 'CLOSED')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    project_status = VALUES(project_status);

INSERT INTO squad_assignment (user_id, project_id, final_decision, proposed_at)
VALUES
    (77001, 101, 'ASSIGNED', NOW()),
    (77001, 102, 'ASSIGNED', NOW())
ON DUPLICATE KEY UPDATE
    final_decision = VALUES(final_decision),
    proposed_at = VALUES(proposed_at);

INSERT INTO project_tech_requirement (project_id, tech_id)
VALUES
    (101, 1),
    (101, 2),
    (102, 3)
ON DUPLICATE KEY UPDATE
    tech_id = VALUES(tech_id);

-- /sql/admin/user_setup.sql  (command/query 공용)

-- 0) 삭제 (FK 고려: 자식 -> 부모)

DELETE es
FROM employee_skill es
JOIN employee e ON e.user_id = es.user_id
JOIN users u ON u.user_id = e.user_id
WHERE u.login_id IN ('newuser', 'newuser2')
   OR u.email IN ('newuser@alloc.co.kr', 'newuser2@alloc.co.kr');

DELETE e
FROM employee e
JOIN users u ON u.user_id = e.user_id
WHERE u.login_id IN ('newuser', 'newuser2')
   OR u.email IN ('newuser@alloc.co.kr', 'newuser2@alloc.co.kr');

DELETE FROM users
WHERE login_id IN ('newuser', 'newuser2')
   OR email IN ('newuser@alloc.co.kr', 'newuser2@alloc.co.kr');

-- dept_id=1 관련 정리
DELETE es
FROM employee_skill es
JOIN employee e ON e.user_id = es.user_id
WHERE e.dept_id = 1;

DELETE FROM employee
WHERE dept_id = 1;

DELETE FROM department
WHERE dept_id = 1;

-- 이번 setup에서 넣을 user_id들 정리
DELETE FROM users
WHERE user_id IN (99001, 77001, 77002, 88001, 77003);

DELETE FROM title_standard WHERE title_standard_id = 1;
DELETE FROM job_standard WHERE job_id = 1;

-- 1) 기준 데이터
INSERT INTO job_standard (job_id, job_name, created_at, updated_at)
VALUES (1, 'BackendDeveloper', NOW(), NOW())
    ON DUPLICATE KEY UPDATE job_name = VALUES(job_name), updated_at = NOW();

INSERT INTO title_standard (title_standard_id, title_name, created_at, updated_at)
VALUES (1, 'Junior', NOW(), NOW())
    ON DUPLICATE KEY UPDATE title_name = VALUES(title_name), updated_at = NOW();

-- 2) users
--  - admin: ADMIN/ACTIVE
--  - kmj: USER/ACTIVE
--  - other: USER/ACTIVE
--  - onlyuser: USER/ACTIVE (employee 없음)
--  - pmuser: PM/SUSPENDED (role/status 필터 검증용)
INSERT INTO users (user_id, login_id, password, user_name, birthday, email, phone, auth, status, profile_img)
VALUES
    (99001, 'admin',    '{noop}admin1234',    '관리자',    '1990-01-01', 'admin@alloc.co.kr',    '010-0000-0000', 'ADMIN', 'ACTIVE',    NULL),
    (77001, 'kmj',      '{noop}password1234', '김명진',    '1997-03-03', 'kmj@alloc.co.kr',      '010-1234-5678', 'USER',  'ACTIVE',    NULL),
    (77002, 'other',    '{noop}password1234', '다른사용자','1996-02-02', 'other@alloc.co.kr',    '010-2222-3333', 'USER',  'ACTIVE',    NULL),
    (88001, 'onlyuser', '{noop}password1234', '직원없음',  '1995-05-05', 'onlyuser@alloc.co.kr', '010-5555-6666', 'USER',  'ACTIVE',    NULL),
    (77003, 'pmuser',   '{noop}password1234', 'PM사용자',  '1994-04-04', 'pm@alloc.co.kr',       '010-3333-4444', 'PM',    'SUSPENDED', NULL);

-- 3) department (manager_id NOT NULL)
INSERT INTO department (dept_id, dept_name, manager_id, is_active, created_at, updated_at)
VALUES (1, '정보보안팀', 77001, TRUE, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
                         dept_name  = VALUES(dept_name),
                         manager_id = VALUES(manager_id),
                         is_active  = VALUES(is_active),
                         updated_at = NOW();

-- 4) employee (onlyuser 제외)
INSERT INTO employee (user_id, job_id, dept_id, title_standard_id, employee_type, hiring_date)
VALUES
    (77001, 1, 1, 1, 'FULL_TIME', '2025-01-01'),
    (77002, 1, 1, 1, 'FULL_TIME', '2025-01-01'),
    (99001, 1, 1, 1, 'FULL_TIME', '2020-01-01'),
    (77003, 1, 1, 1, 'FULL_TIME', '2024-01-01');

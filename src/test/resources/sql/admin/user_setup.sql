-- 0) 삭제 순서: 자식 -> 부모
-- dept_id=1을 참조하는 employee가 남아있으면 department 삭제가 실패하므로
-- dept_id 기준으로 정리

-- (A) 테스트 실행 중 createUser_success 등으로 생성될 수 있는 'newuser' 계정 정리
--     employee(user_id)가 users(user_id)를 참조/공유하므로 employee 먼저 삭제
DELETE e
FROM employee e
JOIN users u ON u.user_id = e.user_id
WHERE u.login_id IN ('newuser', 'newuser2')
   OR u.email IN ('newuser@alloc.co.kr', 'newuser2@alloc.co.kr');

-- employee_skill이 employee를 참조한다면 먼저 삭제가 안전 (FK 방향에 맞춰)
DELETE es
FROM employee_skill es
JOIN employee e ON e.user_id = es.user_id
JOIN users u ON u.user_id = e.user_id
WHERE u.login_id IN ('newuser', 'newuser2')
   OR u.email IN ('newuser@alloc.co.kr', 'newuser2@alloc.co.kr');

-- 마지막으로 users 삭제
DELETE FROM users
WHERE login_id IN ('newuser', 'newuser2')
   OR email IN ('newuser@alloc.co.kr', 'newuser2@alloc.co.kr');


-- (B) dept_id=1 데이터 정리

DELETE es
FROM employee_skill es
JOIN employee e ON e.user_id = es.user_id
WHERE e.dept_id = 1;

DELETE FROM employee
WHERE dept_id = 1;

-- department는 users(manager_id)를 참조하므로 users 삭제 전에 department 제거
DELETE FROM department
WHERE dept_id = 1;

-- 위에서 dept_id=1 쪽 정리, 테스트 유저 삭제
DELETE FROM users
WHERE user_id IN (99001, 77001, 77002, 88001);

DELETE FROM title_standard WHERE title_standard_id = 1;
DELETE FROM job_standard WHERE job_id = 1;


-- 1) 기준 데이터 (Job/Title)
INSERT INTO job_standard (job_id, job_name, created_at, updated_at)
VALUES (1, 'BackendDeveloper', NOW(), NOW())
    ON DUPLICATE KEY UPDATE job_name = VALUES(job_name), updated_at = NOW();

INSERT INTO title_standard (title_standard_id, title_name, created_at, updated_at)
VALUES (1, 'Junior', NOW(), NOW())
    ON DUPLICATE KEY UPDATE title_name = VALUES(title_name), updated_at = NOW();


-- 2) 사용자 데이터 (admin / kmj / other / onlyuser)
INSERT INTO users (user_id, login_id, password, user_name, birthday, email, phone, auth, status, profile_img)
VALUES
    (99001, 'admin',    '{noop}admin1234',    '관리자',    '1990-01-01', 'admin@alloc.co.kr',    '010-0000-0000', 'ADMIN', 'ACTIVE', NULL),
    (77001, 'kmj',      '{noop}password1234', '김명진',    '1997-03-03', 'kmj@alloc.co.kr',      '010-1234-5678', 'USER',  'ACTIVE', NULL),
    (77002, 'other',    '{noop}password1234', '다른사용자','1996-02-02', 'other@alloc.co.kr',    '010-2222-3333', 'USER',  'ACTIVE', NULL),
    (88001, 'onlyuser', '{noop}password1234', '직원없음',  '1995-05-05', 'onlyuser@alloc.co.kr', '010-5555-6666', 'USER',  'ACTIVE', NULL);


-- 3) department (manager_id NOT NULL) : manager로 77001을 지정
INSERT INTO department (dept_id, dept_name, manager_id, created_at, updated_at)
VALUES (1, 'Dev', 77001, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
                         dept_name   = VALUES(dept_name),
                         manager_id  = VALUES(manager_id),
                         updated_at  = NOW();


-- 4) employee
INSERT INTO employee (user_id, job_id, dept_id, title_standard_id, employee_type, hiring_date)
VALUES
    (77001, 1, 1, 1, 'FULL_TIME', '2025-01-01'),
    (77002, 1, 1, 1, 'FULL_TIME', '2025-01-01'),
    (99001, 1, 1, 1, 'FULL_TIME', '2020-01-01');

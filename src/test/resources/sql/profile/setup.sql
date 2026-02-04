-- =========================================
-- profile/setup.sql
-- =========================================

-- 기존 데이터 정리 (테스트 대상 ID만 정리)
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM project_tech_requirement WHERE project_id IN (101, 102);
DELETE FROM squad_assignment WHERE user_id IN (77001, 77002, 77003);
DELETE FROM employee_skill WHERE user_id IN (77001, 77002, 77003);
DELETE FROM employee WHERE user_id IN (77001, 77002, 77003);
DELETE FROM project WHERE project_id IN (101, 102);
DELETE FROM users WHERE user_id IN (77001, 77002, 77003);

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- profile/setup.sql (FIXED: department.manager_id)
-- =========================================

-- 1) 사용자 3명 (부서장 역할: 77001)
INSERT INTO users (user_id, login_id, password, user_name, email, phone, status, auth)
VALUES
    (77001, 'kmj',       'pass', '김명진',     'kmj@alloc.co.kr',       '010-1234-5678', 'ACTIVE', 'USER'),
    (77002, 'nostack',   'pass', '기술스택없음', 'nostack@alloc.co.kr', '010-0000-0000', 'ACTIVE', 'USER'),
    (77003, 'nohistory', 'pass', '히스토리없음', 'nohistory@alloc.co.kr','010-1111-1111', 'ACTIVE', 'USER');


-- 2) 기준 데이터(부서/직군/직급)
-- department.manager_id가 NOT NULL이므로 부서장(user_id=77001)을 지정
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

-- 3) employee (users와 1:1)
-- job_id는 summary에서 JOIN(job_standard)하므로 77001은 반드시 값 존재해야 함
-- title_standard 컬럼명: e.title_standard 를 mapper에서 사용
INSERT INTO employee (user_id, job_id, dept_id, employee_type, title_standard_id, hiring_date)
VALUES
    (77001, 1, 1, 'FULL_TIME', 1, DATE '2022-03-01'),
    (77002, 1, 1, 'FULL_TIME', 1, DATE '2023-01-01'),
    (77003, 1, 1, 'FULL_TIME', 1, DATE '2024-01-01');

-- 4) 기술 표준
-- (tech_standard에 created_at/updated_at NOT NULL이면 아래처럼 컬럼을 추가해야 함)
INSERT INTO tech_standard (tech_id, tech_name, created_at, updated_at)
VALUES
    (1, 'Java',   NOW(), NOW()),
    (2, 'Spring', NOW(), NOW()),
    (3, 'JPA',    NOW(), NOW()),
    (4, 'Python', NOW(), NOW())
    ON DUPLICATE KEY UPDATE
                         tech_name = VALUES(tech_name),
                         updated_at = VALUES(updated_at);

-- 5) 직원 기술 (employee_skill)
INSERT INTO employee_skill (employee_tech_id, user_id, tech_id, proficiency, created_at, updated_at)
VALUES
    (1001, 77001, 1, 'LV3', NOW(), NOW()),
    (1002, 77001, 2, 'LV2', NOW(), NOW()),
    (1003, 77001, 3, 'LV1', NOW(), NOW()),
    (1004, 77001, 4, 'LV2', NOW(), NOW());


-- 6) 프로젝트 2개 + 1개는 ACTIVE(assignedNow true 용)
INSERT INTO project (project_id, name, start_date, end_date, project_status)
VALUES
    (101, 'Project A', DATE '2023-01-01', DATE '2023-12-31', 'ACTIVE'),
    (102, 'Project B', DATE '2022-01-01', DATE '2022-12-31', 'CLOSED');

-- 7) squad_assignment
INSERT INTO squad_assignment (user_id, project_id, final_decision, proposed_at)
VALUES
    (77001, 101, 'ASSIGNED', NOW()),
    (77001, 102, 'ASSIGNED', NOW());


-- 8) project_tech_requirement
INSERT INTO project_tech_requirement (project_id, tech_id)
VALUES
    (101, 1),
    (101, 2),
    (102, 3);

-- 9) 77003(히스토리 없음), 77002(스택 없음)는
-- squad_assignment / employee_skill에 넣지 않았으므로 각각 “빈 결과” 테스트가 성립

-- =========================================
-- profile/cleanup.sql
-- =========================================

-- 1) 프로젝트 기술 요구사항
DELETE FROM project_tech_requirement
WHERE project_id IN (101, 102);

-- 2) 스쿼드 배정
DELETE FROM squad_assignment
WHERE user_id IN (77001, 77002, 77003);

-- 3) 직원 기술
DELETE FROM employee_skill
WHERE user_id IN (77001, 77002, 77003);

-- 4) 직원
DELETE FROM employee
WHERE user_id IN (77001, 77002, 77003);

-- 5) 프로젝트
DELETE FROM project
WHERE project_id IN (101, 102);

-- 6) 기술 표준
DELETE FROM tech_standard
WHERE tech_id IN (1, 2, 3, 4);

-- 7) 직급 / 직군 / 부서
DELETE FROM title_standard
WHERE title_standard = 1;

DELETE FROM job_standard
WHERE job_id = 1;

DELETE FROM department
WHERE dept_id = 1;

-- 8) 사용자 (마지막)
DELETE FROM users
WHERE user_id IN (77001, 77002, 77003);

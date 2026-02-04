-- =========================================
-- profile/cleanup.sql
-- =========================================

SET FOREIGN_KEY_CHECKS = 0;

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

-- 6) 사용자 (마지막)
DELETE FROM users
WHERE user_id IN (77001, 77002, 77003);

SET FOREIGN_KEY_CHECKS = 1;

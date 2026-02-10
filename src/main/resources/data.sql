-- =========================================
-- Moirai 더미데이터
-- 생성일: 2025-01-27
-- =========================================
-- 실행 순서: 이 파일 전체를 순서대로 실행하면 됩니다.
-- 기존 테스트 데이터와 ID 충돌 방지를 위해 77xxx, 99xxx 대역 사용
-- =========================================

SET FOREIGN_KEY_CHECKS = 0;

-- =========================================
-- 1. 기존 더미데이터 정리 (Clean Up)
-- =========================================

-- report / meeting / calendar / gantt 정리
DELETE FROM issue_blockers WHERE issue_blockers_id BETWEEN 99001 AND 99100;
DELETE FROM weekly_tasks WHERE weekly_tasks_id BETWEEN 99001 AND 99100;
DELETE FROM weekly_report_log WHERE project_id BETWEEN 99001 AND 99100;
DELETE FROM weekly_report WHERE report_id BETWEEN 99001 AND 99100;

DELETE FROM participants WHERE participants_id BETWEEN 99001 AND 99100;
DELETE FROM agenda WHERE agenda_id BETWEEN 99001 AND 99100;
DELETE FROM meeting_record_log WHERE project_id BETWEEN 99001 AND 99100;
DELETE FROM meeting_record WHERE meeting_id BETWEEN 99001 AND 99100;

DELETE FROM public_events_member WHERE public_event_id BETWEEN 99001 AND 99100;
DELETE FROM events_log WHERE event_log_id BETWEEN 99001 AND 99100;
DELETE FROM events WHERE event_id BETWEEN 99001 AND 99100;

DELETE FROM task WHERE task_id BETWEEN 99001 AND 99100;
DELETE FROM milestone WHERE milestone_id BETWEEN 99001 AND 99100;

-- squad_assignment 정리
DELETE FROM squad_assignment WHERE user_id BETWEEN 77001 AND 77100;
DELETE FROM squad_assignment WHERE project_id BETWEEN 99001 AND 99100;

-- project_tech_requirement / project_job_requirement 정리
DELETE FROM project_tech_requirement WHERE project_id BETWEEN 99001 AND 99100;
DELETE FROM project_job_requirement WHERE project_id BETWEEN 99001 AND 99100;

-- employee_skill 정리
DELETE FROM employee_skill WHERE user_id BETWEEN 77001 AND 77100;

-- employee 정리
DELETE FROM employee WHERE user_id BETWEEN 77001 AND 77100;

-- project 정리
DELETE FROM project WHERE project_id BETWEEN 99001 AND 99100;

-- department 정리 (manager_id FK 때문에 users보다 먼저)
DELETE FROM department WHERE dept_id BETWEEN 99001 AND 99100;

-- users 정리
DELETE FROM users WHERE user_id BETWEEN 77001 AND 77100;

-- 기준 데이터 정리
DELETE FROM tech_standard WHERE tech_id BETWEEN 99001 AND 99100;
DELETE FROM job_standard WHERE job_id BETWEEN 99001 AND 99100;
DELETE FROM title_standard WHERE title_standard_id BETWEEN 99001 AND 99100;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================
-- 2. 기준 데이터 (Standard Data)
-- =========================================

-- 2-1. 직급 (title_standard) - 5개
INSERT INTO title_standard (title_standard_id, title_name, monthly_cost, created_at, updated_at) VALUES
(99001, '사원', 3500000, NOW(), NOW()),
(99002, '대리', 4500000, NOW(), NOW()),
(99003, '과장', 5500000, NOW(), NOW()),
(99004, '차장', 6500000, NOW(), NOW()),
(99005, '부장', 8000000, NOW(), NOW());

-- 2-2. 직군 (job_standard) - 5개
INSERT INTO job_standard (job_id, job_name, created_at, updated_at) VALUES
(99001, 'Backend', NOW(), NOW()),
(99002, 'Frontend', NOW(), NOW()),
(99003, 'DevOps', NOW(), NOW()),
(99004, 'QA', NOW(), NOW()),
(99005, 'Designer', NOW(), NOW());

-- 2-3. 기술스택 (tech_standard) - 20개
INSERT INTO tech_standard (tech_id, tech_name, created_at, updated_at) VALUES
-- Backend 기술
(99001, 'Java', NOW(), NOW()),
(99002, 'Spring Boot', NOW(), NOW()),
(99003, 'JPA', NOW(), NOW()),
(99004, 'MyBatis', NOW(), NOW()),
(99005, 'Python', NOW(), NOW()),
(99006, 'Node.js', NOW(), NOW()),
(99007, 'Go', NOW(), NOW()),
-- Frontend 기술
(99008, 'React', NOW(), NOW()),
(99009, 'Vue.js', NOW(), NOW()),
(99010, 'TypeScript', NOW(), NOW()),
(99011, 'Next.js', NOW(), NOW()),
(99012, 'HTML/CSS', NOW(), NOW()),
-- DevOps 기술
(99013, 'Docker', NOW(), NOW()),
(99014, 'Kubernetes', NOW(), NOW()),
(99015, 'AWS', NOW(), NOW()),
(99016, 'Jenkins', NOW(), NOW()),
-- Database
(99017, 'MySQL', NOW(), NOW()),
(99018, 'PostgreSQL', NOW(), NOW()),
(99019, 'Redis', NOW(), NOW()),
(99020, 'MongoDB', NOW(), NOW());

-- =========================================
-- 3. 사용자 (users) - 20명
-- =========================================
-- 권한 분포: ADMIN 1명, PM 3명, USER 16명
-- 상태 분포: ACTIVE 18명, SUSPENDED 1명, DELETED 1명

INSERT INTO users (user_id, login_id, password, user_name, birthday, email, phone, auth, status, profile_img) VALUES
-- 관리자
(77001, 'admin.kim', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '김관리', '1980-03-15', 'admin.kim@moirai.co.kr', '010-1000-0001', 'ADMIN', 'ACTIVE', NULL),

-- PM (3명)
(77002, 'pm.lee', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '이프로', '1985-07-22', 'pm.lee@moirai.co.kr', '010-2000-0001', 'PM', 'ACTIVE', NULL),
(77003, 'pm.park', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '박매니', '1983-11-08', 'pm.park@moirai.co.kr', '010-2000-0002', 'PM', 'ACTIVE', NULL),
(77004, 'pm.choi', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '최리더', '1982-05-30', 'pm.choi@moirai.co.kr', '010-2000-0003', 'PM', 'ACTIVE', NULL),

-- Backend 개발자 (5명)
(77005, 'back.kim', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '김백엔', '1992-01-10', 'back.kim@moirai.co.kr', '010-3000-0001', 'USER', 'ACTIVE', NULL),
(77006, 'back.lee', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '이서버', '1994-04-25', 'back.lee@moirai.co.kr', '010-3000-0002', 'USER', 'ACTIVE', NULL),
(77007, 'back.park', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '박스프링', '1990-08-12', 'back.park@moirai.co.kr', '010-3000-0003', 'USER', 'ACTIVE', NULL),
(77008, 'back.jung', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '정자바', '1995-12-03', 'back.jung@moirai.co.kr', '010-3000-0004', 'USER', 'ACTIVE', NULL),
(77009, 'back.han', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '한코딩', '1991-06-18', 'back.han@moirai.co.kr', '010-3000-0005', 'USER', 'ACTIVE', NULL),

-- Frontend 개발자 (4명)
(77010, 'front.kim', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '김프론트', '1993-02-28', 'front.kim@moirai.co.kr', '010-4000-0001', 'USER', 'ACTIVE', NULL),
(77011, 'front.lee', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '이리액트', '1996-09-14', 'front.lee@moirai.co.kr', '010-4000-0002', 'USER', 'ACTIVE', NULL),
(77012, 'front.choi', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '최뷰어', '1994-11-22', 'front.choi@moirai.co.kr', '010-4000-0003', 'USER', 'ACTIVE', NULL),
(77013, 'front.yoon', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '윤타입', '1997-03-07', 'front.yoon@moirai.co.kr', '010-4000-0004', 'USER', 'ACTIVE', NULL),

-- DevOps 엔지니어 (3명)
(77014, 'devops.kim', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '김데옵스', '1988-10-05', 'devops.kim@moirai.co.kr', '010-5000-0001', 'USER', 'ACTIVE', NULL),
(77015, 'devops.lee', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '이클라우드', '1991-07-19', 'devops.lee@moirai.co.kr', '010-5000-0002', 'USER', 'ACTIVE', NULL),
(77016, 'devops.park', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '박쿠버', '1989-04-11', 'devops.park@moirai.co.kr', '010-5000-0003', 'USER', 'SUSPENDED', NULL),

-- QA 엔지니어 (2명)
(77017, 'qa.kim', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '김품질', '1992-08-30', 'qa.kim@moirai.co.kr', '010-6000-0001', 'USER', 'ACTIVE', NULL),
(77018, 'qa.lee', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '이테스트', '1995-01-25', 'qa.lee@moirai.co.kr', '010-6000-0002', 'USER', 'ACTIVE', NULL),

-- Designer (2명)
(77019, 'design.kim', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '김디자인', '1993-05-17', 'design.kim@moirai.co.kr', '010-7000-0001', 'USER', 'ACTIVE', NULL),
(77020, 'design.lee', '$2y$04$quU65kle6JK7xADCSP.aVeH11FiiahGeoRxKbP7uYa2jk9YMErXka', '이유엑스', '1996-12-09', 'design.lee@moirai.co.kr', '010-7000-0002', 'USER', 'DELETED', NULL);

-- =========================================
-- 4. 부서 (department) - 6개
-- =========================================
-- manager_id는 반드시 users에 존재해야 함

INSERT INTO department (dept_id, dept_name, manager_id, is_active, parent_dept_id, created_at, updated_at) VALUES
(99001, '경영지원본부', 77001, TRUE, NULL, NOW(), NOW()),
(99002, '개발1팀', 77002, TRUE, NULL, NOW(), NOW()),
(99003, '개발2팀', 77003, TRUE, NULL, NOW(), NOW()),
(99004, '인프라팀', 77004, TRUE, NULL, NOW(), NOW()),
(99005, 'QA팀', 77001, TRUE, NULL, NOW(), NOW()),
(99006, '디자인팀', 77001, TRUE, NULL, NOW(), NOW());

-- =========================================
-- 5. 직원 (employee) - users와 1:1 매핑
-- =========================================
-- user_id가 PK이자 FK (users.user_id)

INSERT INTO employee (user_id, job_id, dept_id, title_standard_id, employee_type, hiring_date, project_no) VALUES
-- 관리자/PM
(77001, 99001, 99001, 99005, 'FULL_TIME', '2015-03-01', NULL),  -- 김관리 (부장)
(77002, 99001, 99002, 99004, 'FULL_TIME', '2017-06-15', NULL),  -- 이프로 PM (차장)
(77003, 99001, 99003, 99004, 'FULL_TIME', '2018-01-10', NULL),  -- 박매니 PM (차장)
(77004, 99003, 99004, 99003, 'FULL_TIME', '2019-04-01', NULL),  -- 최리더 PM (과장)

-- Backend 개발자
(77005, 99001, 99002, 99003, 'FULL_TIME', '2020-03-02', NULL),  -- 김백엔 (과장)
(77006, 99001, 99002, 99002, 'FULL_TIME', '2021-07-01', NULL),  -- 이서버 (대리)
(77007, 99001, 99002, 99003, 'FULL_TIME', '2019-09-15', NULL),  -- 박스프링 (과장)
(77008, 99001, 99003, 99001, 'FULL_TIME', '2023-01-02', NULL),  -- 정자바 (사원)
(77009, 99001, 99003, 99002, 'FULL_TIME', '2022-04-11', NULL),  -- 한코딩 (대리)

-- Frontend 개발자
(77010, 99002, 99002, 99002, 'FULL_TIME', '2021-05-03', NULL),  -- 김프론트 (대리)
(77011, 99002, 99002, 99001, 'FULL_TIME', '2023-08-21', NULL),  -- 이리액트 (사원)
(77012, 99002, 99003, 99002, 'FULL_TIME', '2022-02-14', NULL),  -- 최뷰어 (대리)
(77013, 99002, 99003, 99001, 'CONTRACT', '2024-01-08', NULL),   -- 윤타입 (사원, 계약직)

-- DevOps 엔지니어
(77014, 99003, 99004, 99003, 'FULL_TIME', '2018-11-05', NULL),  -- 김데옵스 (과장)
(77015, 99003, 99004, 99002, 'FULL_TIME', '2021-03-22', NULL),  -- 이클라우드 (대리)
(77016, 99003, 99004, 99002, 'FULL_TIME', '2020-08-17', NULL),  -- 박쿠버 (대리, SUSPENDED)

-- QA 엔지니어
(77017, 99004, 99005, 99002, 'FULL_TIME', '2021-09-06', NULL),  -- 김품질 (대리)
(77018, 99004, 99005, 99001, 'INTERN', '2024-06-01', NULL),     -- 이테스트 (사원, 인턴)

-- Designer
(77019, 99005, 99006, 99002, 'FULL_TIME', '2022-01-10', NULL),  -- 김디자인 (대리)
(77020, 99005, 99006, 99001, 'VENDOR', '2023-11-01', NULL);     -- 이유엑스 (사원, 외주)

-- =========================================
-- 6. 직원 기술스택 (employee_skill)
-- =========================================
-- proficiency: LV1(초급), LV2(중급), LV3(고급)

INSERT INTO employee_skill (user_id, tech_id, proficiency, created_at, updated_at) VALUES
-- 77005 김백엔 (Backend 과장) - Java, Spring Boot, JPA, MySQL 전문가
(77005, 99001, 'LV3', NOW(), NOW()),  -- Java
(77005, 99002, 'LV3', NOW(), NOW()),  -- Spring Boot
(77005, 99003, 'LV3', NOW(), NOW()),  -- JPA
(77005, 99017, 'LV2', NOW(), NOW()),  -- MySQL
(77005, 99019, 'LV2', NOW(), NOW()),  -- Redis

-- 77006 이서버 (Backend 대리) - Java, Spring Boot 중급
(77006, 99001, 'LV2', NOW(), NOW()),  -- Java
(77006, 99002, 'LV2', NOW(), NOW()),  -- Spring Boot
(77006, 99004, 'LV2', NOW(), NOW()),  -- MyBatis
(77006, 99017, 'LV2', NOW(), NOW()),  -- MySQL

-- 77007 박스프링 (Backend 과장) - Java, Python 가능
(77007, 99001, 'LV3', NOW(), NOW()),  -- Java
(77007, 99002, 'LV3', NOW(), NOW()),  -- Spring Boot
(77007, 99005, 'LV2', NOW(), NOW()),  -- Python
(77007, 99003, 'LV2', NOW(), NOW()),  -- JPA
(77007, 99018, 'LV2', NOW(), NOW()),  -- PostgreSQL

-- 77008 정자바 (Backend 사원) - 신입
(77008, 99001, 'LV1', NOW(), NOW()),  -- Java
(77008, 99002, 'LV1', NOW(), NOW()),  -- Spring Boot
(77008, 99017, 'LV1', NOW(), NOW()),  -- MySQL

-- 77009 한코딩 (Backend 대리) - Node.js, Go 특화
(77009, 99006, 'LV3', NOW(), NOW()),  -- Node.js
(77009, 99007, 'LV2', NOW(), NOW()),  -- Go
(77009, 99001, 'LV2', NOW(), NOW()),  -- Java
(77009, 99020, 'LV2', NOW(), NOW()),  -- MongoDB

-- 77010 김프론트 (Frontend 대리) - React 전문가
(77010, 99008, 'LV3', NOW(), NOW()),  -- React
(77010, 99010, 'LV3', NOW(), NOW()),  -- TypeScript
(77010, 99011, 'LV2', NOW(), NOW()),  -- Next.js
(77010, 99012, 'LV3', NOW(), NOW()),  -- HTML/CSS

-- 77011 이리액트 (Frontend 사원) - React 초급
(77011, 99008, 'LV1', NOW(), NOW()),  -- React
(77011, 99010, 'LV1', NOW(), NOW()),  -- TypeScript
(77011, 99012, 'LV2', NOW(), NOW()),  -- HTML/CSS

-- 77012 최뷰어 (Frontend 대리) - Vue.js 전문가
(77012, 99009, 'LV3', NOW(), NOW()),  -- Vue.js
(77012, 99010, 'LV2', NOW(), NOW()),  -- TypeScript
(77012, 99012, 'LV3', NOW(), NOW()),  -- HTML/CSS
(77012, 99008, 'LV1', NOW(), NOW()),  -- React

-- 77013 윤타입 (Frontend 사원, 계약직) - TypeScript 특화
(77013, 99010, 'LV2', NOW(), NOW()),  -- TypeScript
(77013, 99008, 'LV2', NOW(), NOW()),  -- React
(77013, 99012, 'LV2', NOW(), NOW()),  -- HTML/CSS

-- 77014 김데옵스 (DevOps 과장) - 인프라 전문가
(77014, 99013, 'LV3', NOW(), NOW()),  -- Docker
(77014, 99014, 'LV3', NOW(), NOW()),  -- Kubernetes
(77014, 99015, 'LV3', NOW(), NOW()),  -- AWS
(77014, 99016, 'LV2', NOW(), NOW()),  -- Jenkins

-- 77015 이클라우드 (DevOps 대리)
(77015, 99013, 'LV2', NOW(), NOW()),  -- Docker
(77015, 99014, 'LV2', NOW(), NOW()),  -- Kubernetes
(77015, 99015, 'LV2', NOW(), NOW()),  -- AWS
(77015, 99016, 'LV2', NOW(), NOW()),  -- Jenkins

-- 77016 박쿠버 (DevOps 대리, SUSPENDED)
(77016, 99013, 'LV2', NOW(), NOW()),  -- Docker
(77016, 99014, 'LV3', NOW(), NOW()),  -- Kubernetes
(77016, 99015, 'LV2', NOW(), NOW()),  -- AWS

-- 77017 김품질 (QA 대리) - 테스트 자동화
(77017, 99001, 'LV1', NOW(), NOW()),  -- Java (테스트 코드용)
(77017, 99010, 'LV1', NOW(), NOW()),  -- TypeScript (E2E 테스트용)

-- 77018 이테스트 (QA 사원, 인턴)
(77018, 99012, 'LV1', NOW(), NOW()),  -- HTML/CSS (UI 테스트용)

-- 77019 김디자인 (Designer 대리)
(77019, 99012, 'LV3', NOW(), NOW()),  -- HTML/CSS
(77019, 99008, 'LV1', NOW(), NOW()),  -- React (프로토타입용)

-- 77020 이유엑스 (Designer 사원, 외주/DELETED)
(77020, 99012, 'LV2', NOW(), NOW());  -- HTML/CSS

-- =========================================
-- 7. 프로젝트 (project) - 20개
-- =========================================
-- 상태 분포: DRAFT 3개, ACTIVE 10개, HOLD 2개, CLOSED 5개
-- 타입 분포: NEW, OPERATION, MAINTENANCE 혼합

INSERT INTO project (project_id, name, start_date, end_date, project_status, project_type, description, predicted_cost, partners) VALUES
-- DRAFT (3개) - 기획 중인 프로젝트
(99001, '차세대 ERP 시스템 구축', '2025-04-01', '2026-03-31', 'DRAFT', 'NEW', '전사 ERP 시스템 전면 재구축 프로젝트', 500000000, 'SAP Korea'),
(99002, 'AI 챗봇 도입', '2025-05-01', '2025-10-31', 'DRAFT', 'NEW', '고객 상담용 AI 챗봇 개발', 80000000, NULL),
(99003, '모바일 앱 리뉴얼', '2025-06-01', '2025-12-31', 'DRAFT', 'NEW', '기존 모바일 앱 전면 개편', 120000000, 'Design Studio'),

-- ACTIVE (10개) - 진행 중인 프로젝트
(99004, '통합 인사관리 시스템', '2024-09-01', '2025-08-31', 'ACTIVE', 'NEW', 'HR 업무 통합 관리 시스템 개발', 200000000, NULL),
(99005, '고객 포털 개발', '2024-11-01', '2025-06-30', 'ACTIVE', 'NEW', 'B2B 고객용 셀프서비스 포털', 150000000, 'Cloud Corp'),
(99006, '레거시 시스템 마이그레이션', '2024-07-01', '2025-06-30', 'ACTIVE', 'MAINTENANCE', '기존 시스템의 클라우드 전환', 300000000, 'AWS Korea'),
(99007, '데이터 분석 플랫폼', '2025-01-01', '2025-09-30', 'ACTIVE', 'NEW', '빅데이터 분석 및 시각화 플랫폼', 180000000, NULL),
(99008, '결제 시스템 고도화', '2024-10-01', '2025-04-30', 'ACTIVE', 'OPERATION', '기존 결제 시스템 성능 개선', 90000000, 'PG Company'),
(99009, '보안 인프라 강화', '2025-01-15', '2025-07-31', 'ACTIVE', 'OPERATION', '전사 보안 시스템 업그레이드', 250000000, 'Security Inc'),
(99010, 'API Gateway 구축', '2024-12-01', '2025-05-31', 'ACTIVE', 'NEW', 'MSA 전환을 위한 API Gateway 개발', 100000000, NULL),
(99011, '실시간 모니터링 시스템', '2025-02-01', '2025-08-31', 'ACTIVE', 'NEW', '서비스 모니터링 대시보드 개발', 70000000, NULL),
(99012, '전자결재 시스템 개편', '2024-08-01', '2025-03-31', 'ACTIVE', 'MAINTENANCE', '전자결재 프로세스 개선', 60000000, NULL),
(99013, '물류관리 시스템', '2024-06-01', '2025-05-31', 'ACTIVE', 'NEW', '물류 추적 및 관리 시스템 구축', 220000000, 'Logistics Partner'),

-- HOLD (2개) - 보류 중인 프로젝트
(99014, 'IoT 플랫폼 구축', '2024-05-01', '2025-04-30', 'HOLD', 'NEW', 'IoT 디바이스 연동 플랫폼 (예산 조정 중)', 400000000, 'IoT Solutions'),
(99015, '블록체인 인증 시스템', '2024-08-01', '2025-07-31', 'HOLD', 'NEW', '블록체인 기반 인증 시스템 (기술 검토 중)', 350000000, NULL),

-- CLOSED (5개) - 완료된 프로젝트
(99016, '사내 메신저 개발', '2023-03-01', '2024-02-29', 'CLOSED', 'NEW', '사내 커뮤니케이션 메신저 개발 완료', 80000000, NULL),
(99017, '인트라넷 리뉴얼', '2023-06-01', '2024-05-31', 'CLOSED', 'MAINTENANCE', '사내 인트라넷 UI/UX 개선 완료', 50000000, NULL),
(99018, 'CRM 시스템 도입', '2023-01-01', '2023-12-31', 'CLOSED', 'NEW', '영업관리 CRM 시스템 구축 완료', 180000000, 'CRM Vendor'),
(99019, '백오피스 자동화', '2023-09-01', '2024-08-31', 'CLOSED', 'OPERATION', '관리자 백오피스 업무 자동화 완료', 40000000, NULL),
(99020, 'CI/CD 파이프라인 구축', '2023-04-01', '2023-12-31', 'CLOSED', 'NEW', '개발 배포 자동화 파이프라인 구축 완료', 60000000, NULL);

-- =========================================
-- 8. 프로젝트별 기술 요구사항 (project_tech_requirement)
-- =========================================

INSERT INTO project_tech_requirement (project_id, tech_id, req_level) VALUES
-- 99001 차세대 ERP (DRAFT)
(99001, 99001, 'LV3'),  -- Java
(99001, 99002, 'LV3'),  -- Spring Boot
(99001, 99003, 'LV2'),  -- JPA
(99001, 99008, 'LV2'),  -- React
(99001, 99017, 'LV2'),  -- MySQL

-- 99004 통합 인사관리 시스템 (ACTIVE)
(99004, 99001, 'LV2'),  -- Java
(99004, 99002, 'LV2'),  -- Spring Boot
(99004, 99008, 'LV2'),  -- React
(99004, 99017, 'LV2'),  -- MySQL

-- 99005 고객 포털 개발 (ACTIVE)
(99005, 99006, 'LV2'),  -- Node.js
(99005, 99008, 'LV3'),  -- React
(99005, 99010, 'LV2'),  -- TypeScript
(99005, 99020, 'LV2'),  -- MongoDB

-- 99006 레거시 마이그레이션 (ACTIVE)
(99006, 99001, 'LV3'),  -- Java
(99006, 99013, 'LV3'),  -- Docker
(99006, 99014, 'LV2'),  -- Kubernetes
(99006, 99015, 'LV3'),  -- AWS

-- 99007 데이터 분석 플랫폼 (ACTIVE)
(99007, 99005, 'LV3'),  -- Python
(99007, 99018, 'LV2'),  -- PostgreSQL
(99007, 99008, 'LV2'),  -- React

-- 99008 결제 시스템 고도화 (ACTIVE)
(99008, 99001, 'LV3'),  -- Java
(99008, 99002, 'LV3'),  -- Spring Boot
(99008, 99019, 'LV2'),  -- Redis

-- 99009 보안 인프라 강화 (ACTIVE)
(99009, 99013, 'LV3'),  -- Docker
(99009, 99014, 'LV3'),  -- Kubernetes
(99009, 99015, 'LV3'),  -- AWS
(99009, 99016, 'LV2'),  -- Jenkins

-- 99010 API Gateway 구축 (ACTIVE)
(99010, 99001, 'LV2'),  -- Java
(99010, 99002, 'LV2'),  -- Spring Boot
(99010, 99013, 'LV2'),  -- Docker
(99010, 99015, 'LV2'),  -- AWS

-- 99011 실시간 모니터링 (ACTIVE)
(99011, 99006, 'LV2'),  -- Node.js
(99011, 99008, 'LV2'),  -- React
(99011, 99019, 'LV2'),  -- Redis

-- 99012 전자결재 개편 (ACTIVE)
(99012, 99001, 'LV2'),  -- Java
(99012, 99002, 'LV2'),  -- Spring Boot
(99012, 99009, 'LV2'),  -- Vue.js

-- 99013 물류관리 시스템 (ACTIVE)
(99013, 99001, 'LV2'),  -- Java
(99013, 99002, 'LV2'),  -- Spring Boot
(99013, 99008, 'LV2'),  -- React
(99013, 99017, 'LV2'),  -- MySQL
(99013, 99015, 'LV2'); -- AWS

-- =========================================
-- 9. 프로젝트별 직군 요구사항 (project_job_requirement)
-- =========================================

INSERT INTO project_job_requirement (project_id, job_id, required_count) VALUES
-- 99001 차세대 ERP (DRAFT)
(99001, 99001, 5),  -- Backend 5명
(99001, 99002, 3),  -- Frontend 3명
(99001, 99003, 2),  -- DevOps 2명
(99001, 99004, 2),  -- QA 2명

-- 99004 통합 인사관리 시스템 (ACTIVE)
(99004, 99001, 3),  -- Backend 3명
(99004, 99002, 2),  -- Frontend 2명
(99004, 99004, 1),  -- QA 1명

-- 99005 고객 포털 개발 (ACTIVE)
(99005, 99001, 2),  -- Backend 2명
(99005, 99002, 3),  -- Frontend 3명
(99005, 99005, 1),  -- Designer 1명

-- 99006 레거시 마이그레이션 (ACTIVE)
(99006, 99001, 3),  -- Backend 3명
(99006, 99003, 3),  -- DevOps 3명

-- 99007 데이터 분석 플랫폼 (ACTIVE)
(99007, 99001, 2),  -- Backend 2명
(99007, 99002, 2),  -- Frontend 2명

-- 99008 결제 시스템 고도화 (ACTIVE)
(99008, 99001, 2),  -- Backend 2명
(99008, 99004, 1),  -- QA 1명

-- 99009 보안 인프라 강화 (ACTIVE)
(99009, 99003, 3),  -- DevOps 3명

-- 99010 API Gateway 구축 (ACTIVE)
(99010, 99001, 2),  -- Backend 2명
(99010, 99003, 1),  -- DevOps 1명

-- 99011 실시간 모니터링 (ACTIVE)
(99011, 99001, 1),  -- Backend 1명
(99011, 99002, 2),  -- Frontend 2명

-- 99012 전자결재 개편 (ACTIVE)
(99012, 99001, 2),  -- Backend 2명
(99012, 99002, 1),  -- Frontend 1명

-- 99013 물류관리 시스템 (ACTIVE)
(99013, 99001, 3),  -- Backend 3명
(99013, 99002, 2),  -- Frontend 2명
(99013, 99003, 1),  -- DevOps 1명
(99013, 99004, 1); -- QA 1명

-- =========================================
-- 10. 프로젝트 배정 (squad_assignment)
-- =========================================
-- assignmentStatus: REQUESTED, ACCEPTED, INTERVIEW_REQUESTED
-- finalDecision: PENDING, ASSIGNED, EXCLUDED

INSERT INTO squad_assignment (project_id, user_id, proposed_at, assignment_status, final_decision, decided_at, fitness_score) VALUES
-- 99004 통합 인사관리 시스템 - PM: 이프로(77002)
(99004, 77002, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99004, 77005, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 92),          -- 김백엔 (Backend)
(99004, 77006, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 85),          -- 이서버 (Backend)
(99004, 77010, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 88),          -- 김프론트 (Frontend)
(99004, 77017, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 80),          -- 김품질 (QA)

-- 99005 고객 포털 개발 - PM: 박매니(77003)
(99005, 77003, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99005, 77009, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 95),          -- 한코딩 (Backend - Node.js)
(99005, 77010, NOW(), 'REQUESTED', 'PENDING', NULL, 90),           -- 김프론트 (Frontend) - 검토 중
(99005, 77011, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 78),          -- 이리액트 (Frontend)
(99005, 77019, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 85),          -- 김디자인 (Designer)

-- 99006 레거시 마이그레이션 - PM: 최리더(77004)
(99006, 77004, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99006, 77007, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 90),          -- 박스프링 (Backend)
(99006, 77014, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 95),          -- 김데옵스 (DevOps)
(99006, 77015, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 88),          -- 이클라우드 (DevOps)

-- 99007 데이터 분석 플랫폼
(99007, 77002, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99007, 77007, NOW(), 'INTERVIEW_REQUESTED', 'PENDING', NULL, 82), -- 박스프링 - 인터뷰 요청
(99007, 77012, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 85),          -- 최뷰어 (Frontend)

-- 99008 결제 시스템 고도화
(99008, 77003, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99008, 77005, NOW(), 'REQUESTED', 'PENDING', NULL, 94),           -- 김백엔 - 요청 중 (이미 다른 프로젝트)
(99008, 77008, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 72),          -- 정자바 (Backend 신입)

-- 99009 보안 인프라 강화
(99009, 77004, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99009, 77014, NOW(), 'REQUESTED', 'PENDING', NULL, 96),           -- 김데옵스 - 요청 중
(99009, 77015, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 90),          -- 이클라우드

-- 99010 API Gateway 구축
(99010, 77002, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99010, 77006, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 83),          -- 이서버
(99010, 77015, NOW(), 'INTERVIEW_REQUESTED', 'PENDING', NULL, 80), -- 이클라우드 - 인터뷰 요청

-- 99011 실시간 모니터링
(99011, 77003, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99011, 77009, NOW(), 'REQUESTED', 'PENDING', NULL, 88),           -- 한코딩 - 요청 중
(99011, 77011, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 75),          -- 이리액트
(99011, 77013, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 82),          -- 윤타입

-- 99012 전자결재 개편
(99012, 77004, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99012, 77008, NOW(), 'REQUESTED', 'EXCLUDED', NOW(), 65),         -- 정자바 - 제외됨
(99012, 77009, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 80),          -- 한코딩
(99012, 77012, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 92),          -- 최뷰어 (Vue.js)

-- 99013 물류관리 시스템
(99013, 77002, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), NULL),        -- PM 배정
(99013, 77005, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 90),          -- 김백엔
(99013, 77007, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 87),          -- 박스프링
(99013, 77010, NOW(), 'ACCEPTED', 'ASSIGNED', NOW(), 85),          -- 김프론트
(99013, 77017, NOW(), 'REQUESTED', 'PENDING', NULL, 78),           -- 김품질 - 요청 중

-- CLOSED 프로젝트들 (완료 상태)
-- 99016 사내 메신저 개발 (CLOSED)
(99016, 77002, '2023-03-01', 'ACCEPTED', 'ASSIGNED', '2023-03-01', NULL),
(99016, 77005, '2023-03-05', 'ACCEPTED', 'ASSIGNED', '2023-03-10', 88),
(99016, 77010, '2023-03-05', 'ACCEPTED', 'ASSIGNED', '2023-03-10', 85),

-- 99017 인트라넷 리뉴얼 (CLOSED)
(99017, 77003, '2023-06-01', 'ACCEPTED', 'ASSIGNED', '2023-06-01', NULL),
(99017, 77012, '2023-06-05', 'ACCEPTED', 'ASSIGNED', '2023-06-10', 90),
(99017, 77019, '2023-06-05', 'ACCEPTED', 'ASSIGNED', '2023-06-10', 88),

-- 99020 CI/CD 파이프라인 (CLOSED)
(99020, 77004, '2023-04-01', 'ACCEPTED', 'ASSIGNED', '2023-04-01', NULL),
(99020, 77014, '2023-04-05', 'ACCEPTED', 'ASSIGNED', '2023-04-10', 95),
(99020, 77015, '2023-04-05', 'ACCEPTED', 'ASSIGNED', '2023-04-10', 85);

-- =========================================
-- 11. 마일스톤 (milestone) - 2개
-- =========================================

INSERT INTO milestone (milestone_id, project_id, milestone_name, start_date, end_date, achievement_rate, is_deleted, is_completed, created_at, updated_at) VALUES
(99001, 99001, '요구사항 정의', '2025-04-01', '2025-05-31', 20, FALSE, FALSE, NOW(), NOW()),
(99002, 99001, '핵심 모듈 개발', '2025-06-01', '2025-07-31', 0, FALSE, FALSE, NOW(), NOW());

-- =========================================
-- 12. 태스크 (task) - 4개
-- =========================================

INSERT INTO task (task_id, milestone_id, user_id, task_category, task_name, task_description, task_status, start_date, end_date, is_completed, is_deleted, created_at, updated_at) VALUES
(99001, 99001, 77005, 'DEVELOPMENT', '요구사항 수집', '현업 부서 인터뷰 및 요구사항 수집', 'DONE', '2025-04-01', '2025-04-15', TRUE, FALSE, NOW(), NOW()),
(99002, 99001, 77006, 'DEVELOPMENT', '업무 프로세스 분석', '현행 프로세스 분석 및 개선점 도출', 'INPROGRESS', '2025-04-10', '2025-04-25', FALSE, FALSE, NOW(), NOW()),
(99003, 99002, 77010, 'DEVELOPMENT', '프론트 화면 설계', 'ERP 핵심 화면 와이어프레임 작성', 'TODO', '2025-06-01', '2025-06-30', FALSE, FALSE, NOW(), NOW()),
(99004, 99002, 77007, 'TESTING', '모듈 통합 테스트', '핵심 모듈 통합 테스트 계획 수립', 'TODO', '2025-07-01', '2025-07-15', FALSE, FALSE, NOW(), NOW());

-- =========================================
-- 13. 주간보고 (weekly_report) - 1개
-- =========================================

INSERT INTO weekly_report (report_id, user_id, project_id, week_start_date, week_end_date, report_status, change_of_plan, summary_text, task_completion_rate, is_deleted, created_at, updated_at) VALUES
(99001, 77002, 99001, '2025-04-07', '2025-04-13', 'REVIEWED', '일부 기능 범위가 확정 전이라 일정 재조정 필요', '1주차 요구사항 수집 진행', 0.50, FALSE, NOW(), NOW());

-- =========================================
-- 14. 주간 태스크 (weekly_tasks) - 3개
-- =========================================

INSERT INTO weekly_tasks (weekly_tasks_id, report_id, task_id, task_type, planned_start_date, planned_end_date, is_completed) VALUES
(99001, 99001, 99001, 'COMPLETED', '2025-04-01', '2025-04-15', TRUE),
(99002, 99001, 99002, 'INCOMPLETE', '2025-04-10', '2025-04-25', FALSE),
(99003, 99001, 99003, 'NEXT_WEEK', '2025-06-01', '2025-06-30', FALSE);

-- =========================================
-- 15. 이슈/블로커 (issue_blockers) - 1개
-- =========================================

INSERT INTO issue_blockers (issue_blockers_id, weekly_tasks_id, cause_of_delay, dependency_summary, delayed_dates) VALUES
(99001, 99002, '의사결정 지연', '현업 승인 대기', 3);

-- =========================================
-- 16. 회의록 (meeting_record) - 2개
-- =========================================

INSERT INTO meeting_record (meeting_id, project_id, created_by, progress, meeting_date, meeting_time, is_deleted, created_at, updated_at) VALUES
(99001, 99001, '이프로', 35.0, '2025-04-10 00:00:00', '2025-04-10 10:00:00', FALSE, NOW(), NOW()),
(99002, 99001, '김관리', 40.0, '2025-04-17 00:00:00', '2025-04-17 14:00:00', FALSE, NOW(), NOW());

-- =========================================
-- 17. 회의 안건 (agenda) - 3개
-- =========================================

INSERT INTO agenda (agenda_id, meeting_id, discussion_title, discussion_content, discussion_result, agenda_type) VALUES
(99001, 99001, '요구사항 확정', '주요 기능 목록 검토', '핵심 3개 기능 우선 확정', 'DECISION'),
(99002, 99001, '리스크 점검', '외부 시스템 연동 일정 확인', '연동 담당자 지정', 'RISK'),
(99003, 99002, '개발 일정 조정', '일정 단축 가능 여부 논의', '1주 단축 합의', 'SCHEDULE');

-- =========================================
-- 18. 회의 참가자 (participants) - 5명
-- =========================================

INSERT INTO participants (participants_id, meeting_id, user_id, is_host) VALUES
(99001, 99001, 77002, TRUE),
(99002, 99001, 77005, FALSE),
(99003, 99001, 77010, FALSE),
(99004, 99002, 77001, TRUE),
(99005, 99002, 77006, FALSE);

-- =========================================
-- 19. 일정 (events) - 3개
-- =========================================

INSERT INTO events (event_id, project_id, user_id, event_name, event_state, start_date, end_date, event_type, event_place, event_description, is_deleted, created_at, updated_at) VALUES
(99001, 99001, 77002, '주간보고 작성', 'IN_PROGRESS', '2025-04-07 09:00:00', '2025-04-07 18:00:00', 'PRIVATE', '온라인', '1주차 주간보고 작성', FALSE, NOW(), NOW()),
(99002, 99001, 77001, '정기 회의', 'SUCCESS', '2025-04-10 10:00:00', '2025-04-10 11:00:00', 'PUBLIC', '회의실 A', '주간 정기 회의', FALSE, NOW(), NOW()),
(99003, 99001, 77002, '공유 일정: 데이터 정합성 점검', 'IN_PROGRESS', '2025-04-12 13:00:00', '2025-04-12 15:00:00', 'PUBLIC', '회의실 B', '테스트 데이터 검토', FALSE, NOW(), NOW());

-- =========================================
-- 20. 공개 일정 참가자 (public_events_member) - 4명
-- =========================================

INSERT INTO public_events_member (public_event_id, event_id, user_id) VALUES
(99001, 99002, 77005),
(99002, 99002, 77006),
(99003, 99003, 77010),
(99004, 99003, 77011);

-- =========================================
-- 21. 일정 로그 (events_log) - 3개
-- =========================================

INSERT INTO events_log (event_log_id, event_id, actor_user_id, change_type, log_description, before_start_date, after_start_date, before_end_date, after_end_date, created_at) VALUES
(99001, 99001, 77002, 'CREATE', '주간보고 일정 생성', NULL, '2025-04-07 09:00:00', NULL, '2025-04-07 18:00:00', NOW()),
(99002, 99002, 77001, 'UPDATE', '회의 장소 변경', '2025-04-10 10:00:00', '2025-04-10 10:00:00', '2025-04-10 11:00:00', '2025-04-10 11:00:00', NOW()),
(99003, 99003, 77002, 'CREATE', '공유 일정 생성', NULL, '2025-04-12 13:00:00', NULL, '2025-04-12 15:00:00', NOW());

-- =========================================
-- 22. 주간보고 로그 (weekly_report_log) - 2개
-- =========================================

INSERT INTO weekly_report_log (report_log_id, project_id, report_id, actor_user_id, action_type, log_message, created_at, updated_at) VALUES
(99001, 99001, 99001, 77002, 'CREATE', '주간보고 작성', NOW(), NOW()),
(99002, 99001, 99001, 77002, 'UPDATE', '금주 진행사항 1건, 미완수 1건, 다음주 계획 1건으로 정리', NOW(), NOW());

-- =========================================
-- 23. 회의록 로그 (meeting_record_log) - 2개
-- =========================================

INSERT INTO meeting_record_log (meeting_log_id, project_id, meeting_id, actor_user_id, action_type, log_message, created_at, updated_at) VALUES
(99001, 99001, 99001, 77002, 'CREATE', '회의록 작성', NOW(), NOW()),
(99002, 99001, 99002, 77001, 'UPDATE', '회의 진행률 35.0% -> 40.0%로 변경', NOW(), NOW());

-- =========================================
-- 완료! 데이터 확인용 쿼리
-- =========================================

-- 테이블별 데이터 건수 확인
SELECT 'users' AS table_name, COUNT(*) AS cnt FROM users WHERE user_id BETWEEN 77001 AND 77100
UNION ALL SELECT 'employee', COUNT(*) FROM employee WHERE user_id BETWEEN 77001 AND 77100
UNION ALL SELECT 'department', COUNT(*) FROM department WHERE dept_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'job_standard', COUNT(*) FROM job_standard WHERE job_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'title_standard', COUNT(*) FROM title_standard WHERE title_standard_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'tech_standard', COUNT(*) FROM tech_standard WHERE tech_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'employee_skill', COUNT(*) FROM employee_skill WHERE user_id BETWEEN 77001 AND 77100
UNION ALL SELECT 'project', COUNT(*) FROM project WHERE project_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'project_tech_requirement', COUNT(*) FROM project_tech_requirement WHERE project_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'project_job_requirement', COUNT(*) FROM project_job_requirement WHERE project_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'squad_assignment', COUNT(*) FROM squad_assignment WHERE project_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'milestone', COUNT(*) FROM milestone WHERE milestone_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'task', COUNT(*) FROM task WHERE task_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'weekly_report', COUNT(*) FROM weekly_report WHERE report_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'weekly_tasks', COUNT(*) FROM weekly_tasks WHERE weekly_tasks_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'issue_blockers', COUNT(*) FROM issue_blockers WHERE issue_blockers_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'meeting_record', COUNT(*) FROM meeting_record WHERE meeting_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'agenda', COUNT(*) FROM agenda WHERE agenda_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'participants', COUNT(*) FROM participants WHERE participants_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'events', COUNT(*) FROM events WHERE event_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'public_events_member', COUNT(*) FROM public_events_member WHERE public_event_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'events_log', COUNT(*) FROM events_log WHERE event_log_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'weekly_report_log', COUNT(*) FROM weekly_report_log WHERE report_log_id BETWEEN 99001 AND 99100
UNION ALL SELECT 'meeting_record_log', COUNT(*) FROM meeting_record_log WHERE meeting_log_id BETWEEN 99001 AND 99100;

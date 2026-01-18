DELETE FROM title_standard;

-- 기본 직급 데이터
INSERT INTO title_standard (title_standard_id, title_name, monthly_cost, created_at, updated_at)
VALUES
    (88001, 'Junior', 1000000, NOW(), NOW()),
    (88002, 'Senior', 2000000, NOW(), NOW());

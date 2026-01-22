DELETE FROM title_standard
WHERE title_standard_id IN (99001, 99002, 99003, 99004);

-- 테스트용 직급 데이터 삽입
INSERT INTO title_standard (
    title_standard_id,
    title_name,
    monthly_cost,
    created_at,
    updated_at
) VALUES
      (99001, '사원', 3000000, NOW(), NOW()),
      (99002, '대리', 4000000, NOW(), NOW()),
      (99003, '과장', 5000000, NOW(), NOW()),
      (99004, '차장', 6000000, NOW(), NOW())

    ON DUPLICATE KEY UPDATE
                         title_name   = VALUES(title_name),
                         monthly_cost = VALUES(monthly_cost),
                         updated_at   = VALUES(updated_at);


SET FOREIGN_KEY_CHECKS = 0;

-- 테스트 전용 tech_id 범위만 정리
DELETE FROM tech_standard
WHERE tech_id IN (99001, 99002, 99003, 99004);

DELETE FROM tech_standard
WHERE tech_name IN ('Go', 'Java', 'JPA', 'Docker', 'Python', 'Kotlin');

SET FOREIGN_KEY_CHECKS = 1;

-- 테스트 데이터 upsert
INSERT INTO tech_standard (tech_id, tech_name, created_at, updated_at)
VALUES
    (99001, 'Java',   '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99002, 'JPA',    '2025-01-02 00:00:00', '2025-01-02 00:00:00'),
    (99003, 'Docker', '2025-01-03 00:00:00', '2025-01-03 00:00:00'),
    (99004, 'Python', '2025-01-04 00:00:00', '2025-01-04 00:00:00')
    ON DUPLICATE KEY UPDATE
                         tech_name   = VALUES(tech_name),
                         created_at  = VALUES(created_at),
                         updated_at  = VALUES(updated_at);

INSERT INTO tech_standard (tech_id, tech_name, created_at, updated_at)
VALUES
    (99001, 'Java', '2025-01-01 00:00:00', '2025-01-01 00:00:00'),
    (99002, 'Spring', '2025-01-02 00:00:00', '2025-01-02 00:00:00')
ON DUPLICATE KEY UPDATE
    tech_name = VALUES(tech_name),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

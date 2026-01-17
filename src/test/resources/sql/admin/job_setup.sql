DELETE FROM job_standard WHERE job_id >= 99001;
INSERT INTO job_standard (job_id, job_name,created_at, updated_at) VALUES (99001, 'Backend Developer',NOW(), NOW());
INSERT INTO job_standard (job_id, job_name,created_at, updated_at) VALUES (99002, 'Frontend Developer',NOW(), NOW());
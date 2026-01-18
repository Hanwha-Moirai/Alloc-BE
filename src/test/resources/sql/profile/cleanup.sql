DELETE FROM project_tech_requirement
WHERE project_id IN (101, 102)
  AND tech_id IN (1, 2, 3, 4);

DELETE FROM squad_assignment
WHERE project_id IN (101, 102)
  AND user_id IN (77001, 77002, 77003);

DELETE FROM employee_skill
WHERE user_id IN (77001, 77002, 77003)
  AND tech_id IN (1, 2, 3, 4);

DELETE FROM employee
WHERE user_id IN (77001, 77002, 77003);

DELETE FROM project
WHERE project_id IN (101, 102);

DELETE FROM tech_standard
WHERE tech_id IN (1, 2, 3, 4);

DELETE FROM title_standard
WHERE title_standard_id IN (1);

DELETE FROM job_standard
WHERE job_id IN (1);

DELETE FROM department
WHERE dept_id IN (1);

DELETE FROM users
WHERE user_id IN (77001, 77002, 77003);

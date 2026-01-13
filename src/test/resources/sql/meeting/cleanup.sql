DELETE FROM participants WHERE participants_id IN (88001);
DELETE FROM agenda WHERE agenda_id IN (88001);
DELETE FROM meeting_record WHERE meeting_id IN (88001, 88002);
DELETE FROM squad_assignment WHERE assignment_id IN (88001, 88002);
DELETE FROM project WHERE project_id IN (88001, 88002);
DELETE FROM users WHERE user_id IN (88001, 88002);

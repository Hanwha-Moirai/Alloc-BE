DELETE FROM participants
WHERE meeting_id IN (
    SELECT meeting_id
    FROM meeting_record
    WHERE project_id IN (88001, 88002)
);
DELETE FROM agenda
WHERE meeting_id IN (
    SELECT meeting_id
    FROM meeting_record
    WHERE project_id IN (88001, 88002)
);
DELETE FROM meeting_record WHERE project_id IN (88001, 88002);
DELETE FROM squad_assignment WHERE project_id IN (88001, 88002);
DELETE FROM project WHERE project_id IN (88001, 88002);
DELETE FROM users WHERE user_id IN (88001, 88002);

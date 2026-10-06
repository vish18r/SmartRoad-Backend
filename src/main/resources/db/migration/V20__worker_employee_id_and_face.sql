-- Worker employee IDs (SR1, SR2, ...) and face templates.
--
-- Employee IDs come from a PostgreSQL sequence: nextval() is atomic across concurrent
-- registrations, never hands the same value out twice, and is never rolled back or reused,
-- so an ID belonging to a deleted worker is not issued again. A UNIQUE constraint backs it up.
--
-- face_descriptor holds the 128-value face embedding as JSON; no raw face image is stored.

CREATE SEQUENCE IF NOT EXISTS sr_employee_id_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS employee_id VARCHAR(20);
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS face_descriptor TEXT;

-- An earlier draft stored a per-organization employee_code and a face photo. Drop both.
ALTER TABLE sr_workers DROP COLUMN IF EXISTS employee_code;
ALTER TABLE sr_workers DROP COLUMN IF EXISTS face_photo;

-- Number every existing worker (deleted ones included, so no ID is ever reused) in
-- creation order, then move the sequence past them.
WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY date_created, id) AS rn FROM sr_workers
)
UPDATE sr_workers w SET employee_id = 'SR' || ranked.rn
FROM ranked
WHERE w.id = ranked.id AND w.employee_id IS NULL;

SELECT setval('sr_employee_id_seq',
              GREATEST((SELECT COUNT(*) FROM sr_workers), 1),
              (SELECT COUNT(*) FROM sr_workers) > 0);

ALTER TABLE sr_workers ALTER COLUMN employee_id SET NOT NULL;
ALTER TABLE sr_workers ADD CONSTRAINT uq_sr_workers_employee_id UNIQUE (employee_id);

-- How a worker checked in: QR, FACE or MANUAL.
ALTER TABLE sr_worker_checkins ADD COLUMN IF NOT EXISTS check_in_method VARCHAR(16);

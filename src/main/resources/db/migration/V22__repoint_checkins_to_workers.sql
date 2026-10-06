-- A check-in session belongs to a worker (sr_workers), not to a login user (sr_users).
-- V12 pointed sr_worker_checkins.worker_id at sr_users, so checking in a registered worker failed with
-- "violates foreign key constraint sr_worker_checkins_worker_id_fkey". Repoint it at sr_workers.
--
-- Rows written before this point would reference user ids that are not workers, and would block the new
-- constraint. They were test rows from the old user-as-worker path, so they are removed first.
DELETE FROM sr_worker_checkins WHERE worker_id NOT IN (SELECT id FROM sr_workers);

ALTER TABLE sr_worker_checkins DROP CONSTRAINT IF EXISTS sr_worker_checkins_worker_id_fkey;
ALTER TABLE sr_worker_checkins ADD CONSTRAINT sr_worker_checkins_worker_id_fkey
    FOREIGN KEY (worker_id) REFERENCES sr_workers(id) ON DELETE CASCADE;

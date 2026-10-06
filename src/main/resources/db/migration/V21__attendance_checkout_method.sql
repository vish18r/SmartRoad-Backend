-- Each row of sr_worker_checkins is one attendance session (check-in to check-out).
-- Record how the worker checked out (QR, FACE or MANUAL), next to the existing check_in_method.
ALTER TABLE sr_worker_checkins ADD COLUMN IF NOT EXISTS check_out_method VARCHAR(16);

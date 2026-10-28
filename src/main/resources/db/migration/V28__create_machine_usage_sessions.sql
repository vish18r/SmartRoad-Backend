-- PostgreSQL-only migration (production/dev run on PostgreSQL per the Flyway config in this repo).
-- Tests run against H2, which does not understand EXCLUDE/btree_gist constraints, so the
-- authoritative overlap guard for same-machine sessions lives in the service layer
-- (MachineUtilizationServiceImpl#assertNoOverlap, backed by a JPQL overlap-detection query on
-- MachineUsageSessionRepository). The partial unique index below is a defense-in-depth,
-- best-effort DB-level guard for the common case (no two OPEN sessions for the same machine);
-- it intentionally does not attempt a full EXCLUDE ... USING gist constraint (which would need
-- the btree_gist extension) because the service-layer check must carry the real validation logic
-- in both PostgreSQL and the H2 test database.
CREATE TABLE sr_machine_usage_sessions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    machine_id UUID NOT NULL REFERENCES sr_machines(id),
    project_id UUID REFERENCES sr_projects(id),
    usage_date DATE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    running_hours NUMERIC(10,2),
    idle_hours NUMERIC(10,2),
    operator_worker_id UUID REFERENCES sr_workers(id),
    operator_name VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING',
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    fuel_consumed NUMERIC(12,3),
    fuel_cost NUMERIC(14,2),
    notes TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_mus_time_range CHECK (end_time IS NULL OR end_time > start_time),
    CONSTRAINT chk_sr_mus_running_hours_nonneg CHECK (running_hours IS NULL OR running_hours >= 0),
    CONSTRAINT chk_sr_mus_idle_hours_nonneg CHECK (idle_hours IS NULL OR idle_hours >= 0),
    CONSTRAINT chk_sr_mus_fuel_consumed_nonneg CHECK (fuel_consumed IS NULL OR fuel_consumed >= 0),
    CONSTRAINT chk_sr_mus_fuel_cost_nonneg CHECK (fuel_cost IS NULL OR fuel_cost >= 0)
);

CREATE INDEX idx_sr_mus_machine_usage_date ON sr_machine_usage_sessions(machine_id, usage_date);
CREATE INDEX idx_sr_mus_project_id ON sr_machine_usage_sessions(project_id);
CREATE INDEX idx_sr_mus_start_end_time ON sr_machine_usage_sessions(start_time, end_time);

-- Best-effort DB guard: at most one OPEN (end_time IS NULL) session per machine at a time.
-- Overlap between two COMPLETED sessions for the same machine is still possible to express here
-- without btree_gist, so that case is enforced exclusively by the service-layer overlap query.
CREATE UNIQUE INDEX uq_sr_mus_one_open_session_per_machine
    ON sr_machine_usage_sessions(machine_id)
    WHERE end_time IS NULL AND is_deleted = FALSE;

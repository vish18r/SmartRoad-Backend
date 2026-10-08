-- Shift templates: reusable shift definitions an organization assigns its workers to.
-- total_working_minutes is always backend-computed (overnight-aware) and persisted here so every
-- consumer (attendance enrichment, payroll) reads the same figure instead of re-deriving it.
CREATE TABLE sr_shift_templates (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    shift_code VARCHAR(50) NOT NULL,
    shift_name VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    break_duration_minutes INTEGER NOT NULL DEFAULT 0,
    total_working_minutes INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_shift_templates_break_duration CHECK (break_duration_minutes >= 0),
    CONSTRAINT chk_sr_shift_templates_total_minutes CHECK (total_working_minutes >= 0),
    CONSTRAINT uq_sr_shift_templates_code UNIQUE (shift_code)
);

CREATE INDEX idx_sr_shift_templates_organization_id ON sr_shift_templates(organization_id);
CREATE INDEX idx_sr_shift_templates_status ON sr_shift_templates(status);

-- Worker shift assignments: a worker's assignment to a shift template over a date range. Holds
-- only the worker and shift ids, never worker or shift fields. A row is never deleted; ending an
-- assignment sets effective_to and moves status to ENDED, preserving the history forever.
CREATE TABLE sr_worker_shift_assignments (
    id UUID PRIMARY KEY,
    worker_id UUID NOT NULL REFERENCES sr_workers(id) ON DELETE CASCADE,
    shift_id UUID NOT NULL REFERENCES sr_shift_templates(id) ON DELETE RESTRICT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_worker_shift_assignments_range CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_sr_worker_shift_assignments_worker_id ON sr_worker_shift_assignments(worker_id);
CREATE INDEX idx_sr_worker_shift_assignments_shift_id ON sr_worker_shift_assignments(shift_id);
CREATE INDEX idx_sr_worker_shift_assignments_status ON sr_worker_shift_assignments(status);
CREATE INDEX idx_sr_worker_shift_assignments_effective_range ON sr_worker_shift_assignments(effective_from, effective_to);

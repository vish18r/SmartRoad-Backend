-- Maintenance Management module: preventive and corrective maintenance records for machines.
--
-- Status model: only SCHEDULED, IN_PROGRESS, COMPLETED and CANCELLED are ever persisted in
-- `status`. DUE and OVERDUE are derived on read by MaintenanceServiceImpl from `scheduled_start`,
-- `due_by` and the current time (there is no scheduler/cron infrastructure in this codebase, so
-- nothing would ever flip a persisted DUE/OVERDUE flag). `due_by` is computed at write time as
-- `scheduled_end` when supplied, otherwise the end of the scheduled start's calendar day; it exists
-- so derived-status filters (due/overdue) can be expressed as plain indexed JPQL range predicates.
--
-- Recurrence: a recurring PREVENTIVE record spawns at most ONE pending future occurrence at a time
-- (see docs/MAINTENANCE_MANAGEMENT.md); `parent_maintenance_id` links an occurrence to the record
-- that generated it.
CREATE SEQUENCE IF NOT EXISTS sr_maintenance_ref_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE sr_maintenance_records (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    maintenance_reference VARCHAR(30) NOT NULL,
    machine_id UUID NOT NULL REFERENCES sr_machines(id),
    project_id UUID REFERENCES sr_projects(id),
    title VARCHAR(200) NOT NULL,
    maintenance_type VARCHAR(20) NOT NULL,
    category VARCHAR(40) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    scheduled_start TIMESTAMP WITH TIME ZONE NOT NULL,
    scheduled_end TIMESTAMP WITH TIME ZONE,
    due_by TIMESTAMP WITH TIME ZONE NOT NULL,
    actual_start TIMESTAMP WITH TIME ZONE,
    actual_end TIMESTAMP WITH TIME ZONE,
    reported_at TIMESTAMP WITH TIME ZONE,
    technician_worker_id UUID REFERENCES sr_workers(id),
    technician_name VARCHAR(255),
    estimated_cost NUMERIC(14,2),
    actual_cost NUMERIC(14,2),
    downtime_minutes BIGINT,
    parts_used TEXT,
    notes TEXT,
    cancellation_reason TEXT,
    recurrence_type VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME',
    parent_maintenance_id UUID REFERENCES sr_maintenance_records(id),
    machine_label_before_start VARCHAR(50),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_sr_maintenance_reference UNIQUE (maintenance_reference),
    CONSTRAINT chk_sr_mnt_type CHECK (maintenance_type IN ('PREVENTIVE', 'CORRECTIVE')),
    CONSTRAINT chk_sr_mnt_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_sr_mnt_status CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_sr_mnt_recurrence CHECK (recurrence_type IN ('ONE_TIME', 'DAILY', 'WEEKLY', 'MONTHLY')),
    CONSTRAINT chk_sr_mnt_scheduled_range CHECK (scheduled_end IS NULL OR scheduled_end > scheduled_start),
    CONSTRAINT chk_sr_mnt_actual_range CHECK (actual_end IS NULL OR actual_start IS NULL OR actual_end >= actual_start),
    CONSTRAINT chk_sr_mnt_estimated_cost_nonneg CHECK (estimated_cost IS NULL OR estimated_cost >= 0),
    CONSTRAINT chk_sr_mnt_actual_cost_nonneg CHECK (actual_cost IS NULL OR actual_cost >= 0),
    CONSTRAINT chk_sr_mnt_downtime_nonneg CHECK (downtime_minutes IS NULL OR downtime_minutes >= 0)
);

CREATE INDEX idx_sr_mnt_org_scheduled_start ON sr_maintenance_records(organization_id, scheduled_start);
CREATE INDEX idx_sr_mnt_machine_id ON sr_maintenance_records(machine_id);
CREATE INDEX idx_sr_mnt_project_id ON sr_maintenance_records(project_id);
CREATE INDEX idx_sr_mnt_status_due_by ON sr_maintenance_records(status, due_by);
CREATE INDEX idx_sr_mnt_parent_id ON sr_maintenance_records(parent_maintenance_id);

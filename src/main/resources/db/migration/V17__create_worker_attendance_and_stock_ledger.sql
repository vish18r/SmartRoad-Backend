-- Worker attendance register: one row per worker, per project, per day.
CREATE TABLE sr_worker_attendance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_id UUID NOT NULL REFERENCES sr_workers(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PRESENT',
    hours_worked NUMERIC(6, 2),
    notes TEXT,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    UNIQUE (worker_id, project_id, attendance_date)
);

CREATE INDEX idx_worker_attendance_worker_id ON sr_worker_attendance(worker_id);
CREATE INDEX idx_worker_attendance_project_id ON sr_worker_attendance(project_id);
CREATE INDEX idx_worker_attendance_date ON sr_worker_attendance(attendance_date);

-- Append-only stock movement ledger, one row per material transaction.
CREATE TABLE sr_material_stock_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES sr_materials(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL,
    quantity NUMERIC(18, 3) NOT NULL,
    reference_number VARCHAR(100),
    notes TEXT,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_material_stock_ledger_material_id ON sr_material_stock_ledger(material_id);
CREATE INDEX idx_material_stock_ledger_project_id ON sr_material_stock_ledger(project_id);
CREATE INDEX idx_material_stock_ledger_date_created ON sr_material_stock_ledger(date_created);

-- Reorder threshold used by the low-stock report.
ALTER TABLE sr_materials ADD COLUMN minimum_stock NUMERIC(18, 3);

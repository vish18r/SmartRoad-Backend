CREATE TABLE sr_resource_plans (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES sr_projects(id),
    plan_name VARCHAR(255) NOT NULL,
    plan_date DATE NOT NULL,
    start_date DATE,
    end_date DATE,
    supervisor_worker_id UUID REFERENCES sr_workers(id),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_resource_plans_date_range CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);
CREATE INDEX IF NOT EXISTS idx_sr_resource_plans_project_date ON sr_resource_plans(project_id, plan_date);
CREATE INDEX IF NOT EXISTS idx_sr_resource_plans_organization_id ON sr_resource_plans(organization_id);

CREATE TABLE sr_resource_allocations (
    id UUID PRIMARY KEY,
    resource_plan_id UUID NOT NULL REFERENCES sr_resource_plans(id) ON DELETE CASCADE,
    resource_type VARCHAR(20) NOT NULL,
    resource_id UUID NOT NULL,
    start_time TIME,
    end_time TIME,
    allocation_start_date DATE NOT NULL,
    allocation_end_date DATE,
    planned_quantity NUMERIC(18,3),
    unit VARCHAR(50),
    assigned_worker_id UUID REFERENCES sr_workers(id),
    task_description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    notes TEXT,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_resource_allocations_date_range CHECK (allocation_end_date IS NULL OR allocation_end_date >= allocation_start_date),
    CONSTRAINT chk_sr_resource_allocations_time_range CHECK (end_time IS NULL OR start_time IS NULL OR end_time > start_time)
);
CREATE INDEX IF NOT EXISTS idx_sr_resource_allocations_plan_id ON sr_resource_allocations(resource_plan_id);
CREATE INDEX IF NOT EXISTS idx_sr_resource_allocations_resource ON sr_resource_allocations(resource_type, resource_id, allocation_start_date);

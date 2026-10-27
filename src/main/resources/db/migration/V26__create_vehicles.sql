CREATE TABLE sr_vehicles (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    registration_number VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    current_site_id UUID REFERENCES sr_projects(id),
    driver_worker_id UUID REFERENCES sr_workers(id),
    capacity VARCHAR(100),
    notes TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_sr_vehicles_registration_number UNIQUE (organization_id, registration_number)
);

CREATE INDEX IF NOT EXISTS idx_sr_vehicles_organization_id ON sr_vehicles(organization_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sr_vehicles_current_site_id ON sr_vehicles(current_site_id) WHERE is_deleted = FALSE;

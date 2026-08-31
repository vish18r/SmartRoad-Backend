CREATE TABLE IF NOT EXISTS sr_machines (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    registration_number VARCHAR(50),
    purchase_date DATE,
    purchase_cost NUMERIC(18,2),
    current_site_id UUID REFERENCES projects(id),
    current_value NUMERIC(18,2),
    working_hours NUMERIC(10,2),
    maintenance_status VARCHAR(50),
    fuel_type VARCHAR(50),
    capacity VARCHAR(100),
    operator_name VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sr_machines_org ON sr_machines(organization_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sr_machines_site ON sr_machines(current_site_id) WHERE is_deleted = FALSE;

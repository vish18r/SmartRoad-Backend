CREATE TABLE clients (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    created_by UUID NOT NULL REFERENCES sr_users(id), date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id), date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(255) NOT NULL, contact_person VARCHAR(255), email VARCHAR(255), phone_number VARCHAR(32),
    gst_number VARCHAR(32), address TEXT, active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_clients_organization ON clients(organization_id) WHERE active = TRUE;

CREATE TABLE IF NOT EXISTS sr_workers (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    email_id VARCHAR(255),
    phone_number VARCHAR(20),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(50),
    joining_date DATE,
    assigned_site_id UUID REFERENCES projects(id),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    aadhar_number VARCHAR(50),
    bank_account_number VARCHAR(50),
    bank_name VARCHAR(255),
    ifsc_code VARCHAR(20),
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    experience_years INTEGER,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_sr_workers_org ON sr_workers(organization_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sr_workers_site ON sr_workers(assigned_site_id) WHERE is_deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_sr_workers_email ON sr_workers(email_id) WHERE is_deleted = FALSE;

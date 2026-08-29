CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(200) NOT NULL,
    legal_name VARCHAR(255),
    gst_number VARCHAR(32),
    email VARCHAR(255),
    phone_number VARCHAR(32),
    address TEXT,
    logo_url VARCHAR(2048),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_organizations_gst_number UNIQUE (gst_number)
);

CREATE TABLE organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES sr_users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT uq_organization_membership UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_organization_members_user ON organization_members(user_id) WHERE active = TRUE;

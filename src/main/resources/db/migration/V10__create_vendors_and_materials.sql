-- V10: Create vendors and materials tables

-- Create sr_vendors table
CREATE TABLE sr_vendors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    vendor_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    gst_number VARCHAR(50) UNIQUE,
    vendor_type VARCHAR(50),
    payment_terms VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_vendors_organization_id ON sr_vendors(organization_id);
CREATE INDEX idx_vendors_is_active ON sr_vendors(is_active);

-- Create sr_materials table
CREATE TABLE sr_materials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    material_code VARCHAR(100) NOT NULL UNIQUE,
    material_name VARCHAR(255) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    category VARCHAR(100),
    description TEXT,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_materials_organization_id ON sr_materials(organization_id);
CREATE INDEX idx_materials_material_code ON sr_materials(material_code);

-- Create sr_material_stock table
CREATE TABLE sr_material_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES sr_materials(id) ON DELETE CASCADE,
    quantity_available NUMERIC(18, 3) NOT NULL DEFAULT 0,
    quantity_reserved NUMERIC(18, 3) DEFAULT 0,
    quantity_consumed NUMERIC(18, 3) DEFAULT 0,
    unit_rate NUMERIC(18, 2),
    total_value NUMERIC(18, 2),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    UNIQUE (project_id, material_id)
);

CREATE INDEX idx_material_stock_project_id ON sr_material_stock(project_id);
CREATE INDEX idx_material_stock_material_id ON sr_material_stock(material_id);

-- V9: Create contracts and purchase orders tables

-- Create sr_contracts table
CREATE TABLE sr_contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    contract_number VARCHAR(100) NOT NULL UNIQUE,
    client_id UUID REFERENCES clients(id),
    contractor_id UUID,
    work_order_number VARCHAR(100),
    agreement_number VARCHAR(100),
    contract_value NUMERIC(18, 2),
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    security_deposit NUMERIC(18, 2),
    retention_percentage NUMERIC(5, 2),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    document_reference VARCHAR(500),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_contracts_project_id ON sr_contracts(project_id);
CREATE INDEX idx_contracts_client_id ON sr_contracts(client_id);
CREATE INDEX idx_contracts_status ON sr_contracts(status);

-- Create sr_purchase_orders table
CREATE TABLE sr_purchase_orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    po_number VARCHAR(100) NOT NULL UNIQUE,
    vendor_id UUID NOT NULL,
    material_id UUID NOT NULL,
    quantity NUMERIC(18, 3) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    rate NUMERIC(18, 2) NOT NULL,
    tax_percentage NUMERIC(5, 2),
    total_amount NUMERIC(18, 2),
    order_date TIMESTAMP WITH TIME ZONE,
    expected_delivery_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_purchase_orders_project_id ON sr_purchase_orders(project_id);
CREATE INDEX idx_purchase_orders_vendor_id ON sr_purchase_orders(vendor_id);
CREATE INDEX idx_purchase_orders_material_id ON sr_purchase_orders(material_id);
CREATE INDEX idx_purchase_orders_status ON sr_purchase_orders(status);

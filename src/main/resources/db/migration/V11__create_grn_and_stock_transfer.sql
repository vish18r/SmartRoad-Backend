-- V11: Create GRN and stock transfer tables

-- Create sr_grn table (Goods Received Note)
CREATE TABLE sr_grn (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grn_number VARCHAR(100) NOT NULL UNIQUE,
    purchase_order_id UUID NOT NULL REFERENCES sr_purchase_orders(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES sr_projects(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES sr_materials(id) ON DELETE CASCADE,
    vendor_id UUID NOT NULL,
    ordered_quantity NUMERIC(18, 3) NOT NULL,
    received_quantity NUMERIC(18, 3) NOT NULL,
    rejected_quantity NUMERIC(18, 3) DEFAULT 0,
    receiving_date TIMESTAMP WITH TIME ZONE NOT NULL,
    quality_status VARCHAR(100),
    receiving_notes TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_grn_project_id ON sr_grn(project_id);
CREATE INDEX idx_grn_purchase_order_id ON sr_grn(purchase_order_id);
CREATE INDEX idx_grn_material_id ON sr_grn(material_id);
CREATE INDEX idx_grn_status ON sr_grn(status);

-- Create sr_stock_transfers table
CREATE TABLE sr_stock_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_project_id UUID NOT NULL REFERENCES sr_projects(id) ON DELETE CASCADE,
    destination_project_id UUID NOT NULL REFERENCES sr_projects(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES sr_materials(id) ON DELETE CASCADE,
    quantity_requested NUMERIC(18, 3) NOT NULL,
    quantity_transferred NUMERIC(18, 3),
    transfer_date TIMESTAMP WITH TIME ZONE,
    requested_by UUID,
    approved_by UUID,
    status VARCHAR(50) NOT NULL DEFAULT 'REQUESTED',
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_stock_transfers_source_project_id ON sr_stock_transfers(source_project_id);
CREATE INDEX idx_stock_transfers_destination_project_id ON sr_stock_transfers(destination_project_id);
CREATE INDEX idx_stock_transfers_material_id ON sr_stock_transfers(material_id);
CREATE INDEX idx_stock_transfers_status ON sr_stock_transfers(status);

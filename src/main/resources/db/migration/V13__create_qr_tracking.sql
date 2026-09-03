-- V13: Create QR code tracking tables

-- QR codes for tracking assets, materials, equipment
CREATE TABLE sr_qr_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    qr_code VARCHAR(500) NOT NULL UNIQUE,
    qr_type VARCHAR(50) NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id UUID NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_qr_codes_project ON sr_qr_codes(project_id);
CREATE INDEX idx_qr_codes_qr_code ON sr_qr_codes(qr_code);
CREATE INDEX idx_qr_codes_type ON sr_qr_codes(qr_type);
CREATE INDEX idx_qr_codes_active ON sr_qr_codes(is_active);

-- QR code scan history
CREATE TABLE sr_qr_scans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    qr_code_id UUID NOT NULL REFERENCES sr_qr_codes(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    scanned_by UUID NOT NULL REFERENCES sr_users(id),
    scan_latitude NUMERIC(10, 8),
    scan_longitude NUMERIC(11, 8),
    scan_time TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'SCANNED',
    notes VARCHAR(500),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_qr_scans_qr_code ON sr_qr_scans(qr_code_id);
CREATE INDEX idx_qr_scans_project ON sr_qr_scans(project_id);
CREATE INDEX idx_qr_scans_time ON sr_qr_scans(scan_time DESC);

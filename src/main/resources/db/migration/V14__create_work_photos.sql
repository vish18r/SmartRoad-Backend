-- V14: Create work photos and project evidence tables

-- Work photos (before/after, daily timeline)
CREATE TABLE sr_work_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    photo_type VARCHAR(50) NOT NULL,
    photo_url VARCHAR(2000) NOT NULL,
    photo_date TIMESTAMP WITH TIME ZONE NOT NULL,
    latitude NUMERIC(10, 8),
    longitude NUMERIC(11, 8),
    description VARCHAR(500),
    work_area VARCHAR(255),
    uploaded_by UUID NOT NULL REFERENCES sr_users(id),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_work_photos_project ON sr_work_photos(project_id);
CREATE INDEX idx_work_photos_type ON sr_work_photos(photo_type);
CREATE INDEX idx_work_photos_date ON sr_work_photos(photo_date DESC);
CREATE INDEX idx_work_photos_work_area ON sr_work_photos(work_area);

-- Daily site photo timeline
CREATE TABLE sr_daily_photo_timelines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    timeline_date DATE NOT NULL,
    morning_photo_url VARCHAR(2000),
    afternoon_photo_url VARCHAR(2000),
    evening_photo_url VARCHAR(2000),
    summary VARCHAR(1000),
    weather_condition VARCHAR(100),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    UNIQUE(project_id, timeline_date)
);

CREATE INDEX idx_daily_timeline_project ON sr_daily_photo_timelines(project_id);
CREATE INDEX idx_daily_timeline_date ON sr_daily_photo_timelines(timeline_date DESC);

-- Work completion evidence/certificate
CREATE TABLE sr_work_completion_certificates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    work_section VARCHAR(255) NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_by UUID NOT NULL REFERENCES sr_users(id),
    verified_by UUID REFERENCES sr_users(id),
    verification_date TIMESTAMP WITH TIME ZONE,
    photo_url VARCHAR(2000),
    signature_url VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    remarks VARCHAR(500),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_completion_cert_project ON sr_work_completion_certificates(project_id);
CREATE INDEX idx_completion_cert_status ON sr_work_completion_certificates(status);
CREATE INDEX idx_completion_cert_date ON sr_work_completion_certificates(completion_date DESC);

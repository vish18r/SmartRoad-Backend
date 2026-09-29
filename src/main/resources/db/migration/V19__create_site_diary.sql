CREATE TABLE sr_site_diary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES sr_projects(id) ON DELETE CASCADE,
    diary_date DATE NOT NULL,
    weather VARCHAR(100),
    temperature_celsius NUMERIC(5,2),
    site_conditions TEXT,
    work_summary TEXT,
    issues TEXT,
    safety_notes TEXT,
    notes TEXT,
    created_by UUID REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    UNIQUE (project_id, diary_date)
);
CREATE INDEX idx_site_diary_project_id ON sr_site_diary(project_id);
CREATE INDEX idx_site_diary_date ON sr_site_diary(diary_date);

-- V12: Create GPS tracking tables for site tracking and worker check-in/check-out

-- GPS Location history table
CREATE TABLE sr_gps_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    worker_id UUID REFERENCES sr_users(id) ON DELETE SET NULL,
    latitude NUMERIC(10, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,
    accuracy NUMERIC(8, 2),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_gps_locations_project ON sr_gps_locations(project_id);
CREATE INDEX idx_gps_locations_worker ON sr_gps_locations(worker_id);
CREATE INDEX idx_gps_locations_timestamp ON sr_gps_locations(timestamp DESC);

-- Worker check-in/check-out tracking
CREATE TABLE sr_worker_checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES sr_users(id) ON DELETE CASCADE,
    check_in_time TIMESTAMP WITH TIME ZONE NOT NULL,
    check_in_latitude NUMERIC(10, 8),
    check_in_longitude NUMERIC(11, 8),
    check_in_accuracy NUMERIC(8, 2),
    check_out_time TIMESTAMP WITH TIME ZONE,
    check_out_latitude NUMERIC(10, 8),
    check_out_longitude NUMERIC(11, 8),
    check_out_accuracy NUMERIC(8, 2),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    duration_minutes INTEGER,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_worker_checkins_project ON sr_worker_checkins(project_id);
CREATE INDEX idx_worker_checkins_worker ON sr_worker_checkins(worker_id);
CREATE INDEX idx_worker_checkins_date ON sr_worker_checkins(date_created DESC);
CREATE INDEX idx_worker_checkins_status ON sr_worker_checkins(status);

-- Site geofencing boundaries
CREATE TABLE sr_geofence_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    latitude NUMERIC(10, 8) NOT NULL,
    longitude NUMERIC(11, 8) NOT NULL,
    radius_meters NUMERIC(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_geofence_project ON sr_geofence_zones(project_id);
CREATE INDEX idx_geofence_active ON sr_geofence_zones(is_active);

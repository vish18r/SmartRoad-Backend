-- Initial authentication schema for SmartRoad.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS sr_users (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email_id VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    country_code VARCHAR(10),
    password VARCHAR(255),
    status VARCHAR(50),
    role VARCHAR(50),
    user_type VARCHAR(50),
    email_verified_yn BOOLEAN,
    oauth_signin_id VARCHAR(255),
    oauth_type VARCHAR(50),
    profile_image_url VARCHAR(2048),
    last_login_at TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INTEGER,
    locked_until TIMESTAMP WITH TIME ZONE,
    is_deleted BOOLEAN DEFAULT FALSE,
    end_date TIMESTAMP WITH TIME ZONE,
    registration_source VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS sr_sessions (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    user_id UUID NOT NULL REFERENCES sr_users(id),
    token TEXT NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    device VARCHAR(255),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_sr_sessions_user_id ON sr_sessions(user_id);

CREATE TABLE IF NOT EXISTS sr_otps (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    request_id VARCHAR(255), transaction_id VARCHAR(255), active_yn BOOLEAN NOT NULL DEFAULT TRUE,
    email_id VARCHAR(255), email_otp VARCHAR(10), phone_number VARCHAR(20), phone_otp VARCHAR(10),
    expires_at TIMESTAMP WITH TIME ZONE, callback_raw_data JSONB,
    callback_received_date TIMESTAMP WITH TIME ZONE, delivery_status VARCHAR(50), flow VARCHAR(50),
    message_type VARCHAR(50), retry_count INTEGER NOT NULL DEFAULT 0, vendor_name VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_sr_otps_email_flow ON sr_otps(email_id, flow);

CREATE TABLE IF NOT EXISTS oauth_states (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    state VARCHAR(255) UNIQUE, oauth_type VARCHAR(50), expires_at TIMESTAMP WITH TIME ZONE, used BOOLEAN
);

CREATE TABLE IF NOT EXISTS user_audit_logs (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL,
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    user_id UUID, performed_by UUID, action VARCHAR(100), status VARCHAR(50),
    details TEXT, ip_address VARCHAR(64), user_agent VARCHAR(1024)
);

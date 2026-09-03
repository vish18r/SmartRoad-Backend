-- SmartRoad Database Schema
-- Road Construction & Contractor Management System
-- All tables use sr_ prefix

-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================
-- 1. AUTHENTICATION AND USER MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_users" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "country_code" VARCHAR(10),
    "email_id" VARCHAR(255),
    "phone_number" VARCHAR(20),
    "status" VARCHAR(50) NOT NULL,
    "first_name" VARCHAR(100),
    "last_name" VARCHAR(100),
    "role" VARCHAR(50),
    "user_type" VARCHAR(50),
    "oauth_type" VARCHAR(50),
    "end_date" TIMESTAMPTZ,
    "password" VARCHAR(255),
    "registration_source" VARCHAR(50),
    "email_verified_yn" BOOLEAN DEFAULT FALSE,
    "oauth_signin_id" VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS "uk_sr_users_email_id" ON "sr_users"("email_id") WHERE "email_id" IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "uk_sr_users_phone_number" ON "sr_users"("phone_number") WHERE "phone_number" IS NOT NULL;

CREATE INDEX IF NOT EXISTS "idx_sr_users_status" ON "sr_users"("status");
CREATE INDEX IF NOT EXISTS "idx_sr_users_date_created" ON "sr_users"("date_created");

CREATE TABLE IF NOT EXISTS "sr_sessions" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "user_id" UUID NOT NULL,
    "token" TEXT NOT NULL,
    "status" VARCHAR(50) NOT NULL,
    "device" VARCHAR(255),
    "expires_at" TIMESTAMPTZ NOT NULL,
    "last_used_at" TIMESTAMPTZ,
    "revoked_at" TIMESTAMPTZ,
    
    CONSTRAINT "fk_sr_sessions_user_id" FOREIGN KEY ("user_id") REFERENCES "sr_users"("id") ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS "idx_sr_sessions_user_id_status" ON "sr_sessions"("user_id", "status");
CREATE INDEX IF NOT EXISTS "idx_sr_sessions_expires_at" ON "sr_sessions"("expires_at");

CREATE TABLE IF NOT EXISTS "sr_otps" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "request_id" VARCHAR(255),
    "transaction_id" VARCHAR(255),
    "active_yn" BOOLEAN DEFAULT TRUE,
    "email_id" VARCHAR(255),
    "email_otp" VARCHAR(10),
    "phone_number" VARCHAR(20),
    "phone_otp" VARCHAR(10),
    "expires_at" TIMESTAMPTZ,
    "callback_raw_data" JSONB,
    "callback_received_date" TIMESTAMPTZ,
    "delivery_status" VARCHAR(50),
    "flow" VARCHAR(50),
    "message_type" VARCHAR(50),
    "retry_count" INT DEFAULT 0,
    "vendor_name" VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS "sr_oauth_states" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "user_id" UUID,
    "state" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL,
    "expires_on" TIMESTAMPTZ NOT NULL,
    "used_yn" BOOLEAN DEFAULT FALSE,
    "used_at" TIMESTAMPTZ,
    "registration_source" VARCHAR(50),
    
    CONSTRAINT "fk_sr_oauth_states_user_id" FOREIGN KEY ("user_id") REFERENCES "sr_users"("id") ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS "sr_user_audit_logs" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "action_done_for_user_id" UUID NOT NULL,
    "requested_by" UUID NOT NULL,
    "action_done" VARCHAR(255) NOT NULL,
    "status" VARCHAR(50),
    "pending_user_yn" BOOLEAN DEFAULT FALSE,
    "reason" TEXT,
    
    CONSTRAINT "fk_sr_user_audit_logs_action_done_for_user_id" FOREIGN KEY ("action_done_for_user_id") REFERENCES "sr_users"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_user_audit_logs_requested_by" FOREIGN KEY ("requested_by") REFERENCES "sr_users"("id") ON DELETE RESTRICT
);

-- ============================================
-- 2. ORGANIZATION / CONTRACTOR MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_organizations" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_name" VARCHAR(255) NOT NULL,
    "organization_code" VARCHAR(50) NOT NULL,
    "owner_user_id" UUID NOT NULL,
    "email_id" VARCHAR(255),
    "phone_number" VARCHAR(20),
    "address" TEXT,
    "city" VARCHAR(100),
    "state" VARCHAR(100),
    "country" VARCHAR(100),
    "pincode" VARCHAR(20),
    "gst_number" VARCHAR(50),
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_organizations_owner_user_id" FOREIGN KEY ("owner_user_id") REFERENCES "sr_users"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_organizations_organization_code" UNIQUE ("organization_code")
);

CREATE INDEX IF NOT EXISTS "idx_sr_organizations_status" ON "sr_organizations"("status");

CREATE TABLE IF NOT EXISTS "sr_organization_users" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_organization_users_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_organization_users_user_id" FOREIGN KEY ("user_id") REFERENCES "sr_users"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_organization_users_org_user" UNIQUE ("organization_id", "user_id")
);

-- ============================================
-- 3. PROJECT MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_projects" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "project_code" VARCHAR(50) NOT NULL,
    "project_name" VARCHAR(255) NOT NULL,
    "project_type" VARCHAR(50),
    "client_name" VARCHAR(255),
    "contract_number" VARCHAR(100),
    "description" TEXT,
    "start_date" DATE,
    "expected_end_date" DATE,
    "actual_end_date" DATE,
    "total_budget" NUMERIC(15,2),
    "project_status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_projects_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_projects_org_project_code" UNIQUE ("organization_id", "project_code"),
    CONSTRAINT "chk_sr_projects_total_budget" CHECK ("total_budget" IS NULL OR "total_budget" >= 0),
    CONSTRAINT "chk_sr_projects_dates" CHECK ("actual_end_date" IS NULL OR "start_date" IS NULL OR "actual_end_date" >= "start_date")
);

CREATE INDEX IF NOT EXISTS "idx_sr_projects_organization_id" ON "sr_projects"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_projects_project_status" ON "sr_projects"("project_status");
CREATE INDEX IF NOT EXISTS "idx_sr_projects_start_date" ON "sr_projects"("start_date");

CREATE TABLE IF NOT EXISTS "sr_project_users" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "user_id" UUID NOT NULL,
    "project_role" VARCHAR(50),
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_project_users_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_project_users_user_id" FOREIGN KEY ("user_id") REFERENCES "sr_users"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_project_users_project_user" UNIQUE ("project_id", "user_id")
);

CREATE TABLE IF NOT EXISTS "sr_project_locations" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "address" TEXT,
    "city" VARCHAR(100),
    "state" VARCHAR(100),
    "pincode" VARCHAR(20),
    "latitude" NUMERIC(10,8),
    "longitude" NUMERIC(11,8),
    
    CONSTRAINT "fk_sr_project_locations_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS "idx_sr_project_locations_project_id" ON "sr_project_locations"("project_id");

CREATE TABLE IF NOT EXISTS "sr_project_status_history" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "previous_status" VARCHAR(50),
    "current_status" VARCHAR(50) NOT NULL,
    "remarks" TEXT,
    "changed_by" UUID NOT NULL,
    
    CONSTRAINT "fk_sr_project_status_history_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_project_status_history_changed_by" FOREIGN KEY ("changed_by") REFERENCES "sr_users"("id") ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS "idx_sr_project_status_history_project_id" ON "sr_project_status_history"("project_id");

-- ============================================
-- 4. CC ROAD MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_road_details" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "road_name" VARCHAR(255) NOT NULL,
    "road_type" VARCHAR(50),
    "total_length" NUMERIC(15,2),
    "total_width" NUMERIC(15,2),
    "total_thickness" NUMERIC(15,2),
    "start_point" TEXT,
    "end_point" TEXT,
    "estimated_area" NUMERIC(15,2),
    "estimated_concrete_quantity" NUMERIC(15,2),
    
    CONSTRAINT "fk_sr_road_details_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS "idx_sr_road_details_project_id" ON "sr_road_details"("project_id");

CREATE TABLE IF NOT EXISTS "sr_road_segments" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "road_id" UUID NOT NULL,
    "segment_code" VARCHAR(50),
    "segment_name" VARCHAR(255),
    "start_chainage" NUMERIC(15,2),
    "end_chainage" NUMERIC(15,2),
    "length" NUMERIC(15,2),
    "width" NUMERIC(15,2),
    "thickness" NUMERIC(15,2),
    "status" VARCHAR(50),
    
    CONSTRAINT "fk_sr_road_segments_road_id" FOREIGN KEY ("road_id") REFERENCES "sr_road_details"("id") ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS "idx_sr_road_segments_road_id" ON "sr_road_segments"("road_id");

CREATE TABLE IF NOT EXISTS "sr_road_measurements" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "road_id" UUID NOT NULL,
    "segment_id" UUID,
    "measurement_date" DATE NOT NULL,
    "length" NUMERIC(15,2),
    "width" NUMERIC(15,2),
    "thickness" NUMERIC(15,2),
    "area" NUMERIC(15,2),
    "volume" NUMERIC(15,2),
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_road_measurements_road_id" FOREIGN KEY ("road_id") REFERENCES "sr_road_details"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_road_measurements_segment_id" FOREIGN KEY ("segment_id") REFERENCES "sr_road_segments"("id") ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS "idx_sr_road_measurements_road_id" ON "sr_road_measurements"("road_id");
CREATE INDEX IF NOT EXISTS "idx_sr_road_measurements_segment_id" ON "sr_road_measurements"("segment_id");

-- ============================================
-- 5. WORKER MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_workers" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "first_name" VARCHAR(100) NOT NULL,
    "last_name" VARCHAR(100),
    "phone_number" VARCHAR(20),
    "address" TEXT,
    "worker_type" VARCHAR(50) NOT NULL,
    "daily_wage" NUMERIC(15,2),
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_workers_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_workers_daily_wage" CHECK ("daily_wage" IS NULL OR "daily_wage" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_workers_organization_id" ON "sr_workers"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_workers_worker_type" ON "sr_workers"("worker_type");
CREATE INDEX IF NOT EXISTS "idx_sr_workers_status" ON "sr_workers"("status");

CREATE TABLE IF NOT EXISTS "sr_worker_projects" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "worker_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "assigned_date" DATE NOT NULL,
    "released_date" DATE,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_worker_projects_worker_id" FOREIGN KEY ("worker_id") REFERENCES "sr_workers"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_worker_projects_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_worker_projects_worker_project" UNIQUE ("worker_id", "project_id", "assigned_date")
);

CREATE INDEX IF NOT EXISTS "idx_sr_worker_projects_worker_id" ON "sr_worker_projects"("worker_id");
CREATE INDEX IF NOT EXISTS "idx_sr_worker_projects_project_id" ON "sr_worker_projects"("project_id");

CREATE TABLE IF NOT EXISTS "sr_worker_attendance" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "worker_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "attendance_date" DATE NOT NULL,
    "check_in_time" TIMESTAMPTZ,
    "check_out_time" TIMESTAMPTZ,
    "attendance_status" VARCHAR(50),
    "working_hours" NUMERIC(10,2),
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_worker_attendance_worker_id" FOREIGN KEY ("worker_id") REFERENCES "sr_workers"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_worker_attendance_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_worker_attendance_worker_project_date" UNIQUE ("worker_id", "project_id", "attendance_date"),
    CONSTRAINT "chk_sr_worker_attendance_times" CHECK ("check_out_time" IS NULL OR "check_in_time" IS NULL OR "check_out_time" >= "check_in_time"),
    CONSTRAINT "chk_sr_worker_attendance_working_hours" CHECK ("working_hours" IS NULL OR "working_hours" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_worker_attendance_worker_date" ON "sr_worker_attendance"("worker_id", "attendance_date");
CREATE INDEX IF NOT EXISTS "idx_sr_worker_attendance_project_id" ON "sr_worker_attendance"("project_id");

CREATE TABLE IF NOT EXISTS "sr_worker_wages" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "worker_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "wage_type" VARCHAR(50),
    "daily_wage" NUMERIC(15,2),
    "overtime_rate" NUMERIC(15,2),
    "effective_from" DATE NOT NULL,
    "effective_to" DATE,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_worker_wages_worker_id" FOREIGN KEY ("worker_id") REFERENCES "sr_workers"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_worker_wages_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_worker_wages_daily_wage" CHECK ("daily_wage" IS NULL OR "daily_wage" >= 0),
    CONSTRAINT "chk_sr_worker_wages_overtime_rate" CHECK ("overtime_rate" IS NULL OR "overtime_rate" >= 0),
    CONSTRAINT "chk_sr_worker_wages_dates" CHECK ("effective_to" IS NULL OR "effective_from" IS NULL OR "effective_to" >= "effective_from")
);

CREATE INDEX IF NOT EXISTS "idx_sr_worker_wages_worker_id" ON "sr_worker_wages"("worker_id");
CREATE INDEX IF NOT EXISTS "idx_sr_worker_wages_project_id" ON "sr_worker_wages"("project_id");

CREATE TABLE IF NOT EXISTS "sr_worker_payments" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "worker_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "payment_date" DATE NOT NULL,
    "amount" NUMERIC(15,2) NOT NULL,
    "payment_mode" VARCHAR(50),
    "payment_status" VARCHAR(50) NOT NULL,
    "transaction_reference" VARCHAR(255),
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_worker_payments_worker_id" FOREIGN KEY ("worker_id") REFERENCES "sr_workers"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_worker_payments_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_worker_payments_amount" CHECK ("amount" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_worker_payments_worker_id" ON "sr_worker_payments"("worker_id");
CREATE INDEX IF NOT EXISTS "idx_sr_worker_payments_project_id" ON "sr_worker_payments"("project_id");
CREATE INDEX IF NOT EXISTS "idx_sr_worker_payments_payment_date" ON "sr_worker_payments"("payment_date");

-- ============================================
-- 6. MACHINE AND EQUIPMENT MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_machines" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "machine_code" VARCHAR(50) NOT NULL,
    "machine_name" VARCHAR(255) NOT NULL,
    "machine_type" VARCHAR(50) NOT NULL,
    "registration_number" VARCHAR(50),
    "purchase_date" DATE,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_machines_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_machines_org_machine_code" UNIQUE ("organization_id", "machine_code")
);

CREATE INDEX IF NOT EXISTS "idx_sr_machines_organization_id" ON "sr_machines"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_machines_machine_type" ON "sr_machines"("machine_type");
CREATE INDEX IF NOT EXISTS "idx_sr_machines_status" ON "sr_machines"("status");

CREATE TABLE IF NOT EXISTS "sr_machine_projects" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "machine_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "assigned_date" DATE NOT NULL,
    "released_date" DATE,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_machine_projects_machine_id" FOREIGN KEY ("machine_id") REFERENCES "sr_machines"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_machine_projects_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_machine_projects_machine_project" UNIQUE ("machine_id", "project_id", "assigned_date")
);

CREATE INDEX IF NOT EXISTS "idx_sr_machine_projects_machine_id" ON "sr_machine_projects"("machine_id");
CREATE INDEX IF NOT EXISTS "idx_sr_machine_projects_project_id" ON "sr_machine_projects"("project_id");

CREATE TABLE IF NOT EXISTS "sr_machine_usage" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "machine_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "operator_id" UUID,
    "usage_date" DATE NOT NULL,
    "start_time" TIMESTAMPTZ,
    "end_time" TIMESTAMPTZ,
    "running_hours" NUMERIC(10,2),
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_machine_usage_machine_id" FOREIGN KEY ("machine_id") REFERENCES "sr_machines"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_machine_usage_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_machine_usage_operator_id" FOREIGN KEY ("operator_id") REFERENCES "sr_workers"("id") ON DELETE SET NULL,
    CONSTRAINT "chk_sr_machine_usage_running_hours" CHECK ("running_hours" IS NULL OR "running_hours" >= 0),
    CONSTRAINT "chk_sr_machine_usage_times" CHECK ("end_time" IS NULL OR "start_time" IS NULL OR "end_time" >= "start_time")
);

CREATE INDEX IF NOT EXISTS "idx_sr_machine_usage_machine_date" ON "sr_machine_usage"("machine_id", "usage_date");
CREATE INDEX IF NOT EXISTS "idx_sr_machine_usage_project_id" ON "sr_machine_usage"("project_id");

CREATE TABLE IF NOT EXISTS "sr_machine_maintenance" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "machine_id" UUID NOT NULL,
    "maintenance_type" VARCHAR(50) NOT NULL,
    "maintenance_date" DATE NOT NULL,
    "cost" NUMERIC(15,2),
    "description" TEXT,
    "next_maintenance_date" DATE,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_machine_maintenance_machine_id" FOREIGN KEY ("machine_id") REFERENCES "sr_machines"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_machine_maintenance_cost" CHECK ("cost" IS NULL OR "cost" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_machine_maintenance_machine_id" ON "sr_machine_maintenance"("machine_id");
CREATE INDEX IF NOT EXISTS "idx_sr_machine_maintenance_maintenance_date" ON "sr_machine_maintenance"("maintenance_date");

-- ============================================
-- 7. FUEL MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_fuel_logs" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "machine_id" UUID,
    "fuel_type" VARCHAR(50) NOT NULL,
    "quantity" NUMERIC(15,2) NOT NULL,
    "cost_per_unit" NUMERIC(15,2) NOT NULL,
    "total_amount" NUMERIC(15,2) NOT NULL,
    "fuel_date" DATE NOT NULL,
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_fuel_logs_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_fuel_logs_machine_id" FOREIGN KEY ("machine_id") REFERENCES "sr_machines"("id") ON DELETE SET NULL,
    CONSTRAINT "chk_sr_fuel_logs_quantity" CHECK ("quantity" >= 0),
    CONSTRAINT "chk_sr_fuel_logs_cost_per_unit" CHECK ("cost_per_unit" >= 0),
    CONSTRAINT "chk_sr_fuel_logs_total_amount" CHECK ("total_amount" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_fuel_logs_project_id" ON "sr_fuel_logs"("project_id");
CREATE INDEX IF NOT EXISTS "idx_sr_fuel_logs_machine_id" ON "sr_fuel_logs"("machine_id");
CREATE INDEX IF NOT EXISTS "idx_sr_fuel_logs_fuel_date" ON "sr_fuel_logs"("fuel_date");

-- ============================================
-- 8. MATERIAL MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_materials" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "material_code" VARCHAR(50) NOT NULL,
    "material_name" VARCHAR(255) NOT NULL,
    "material_type" VARCHAR(50) NOT NULL,
    "unit" VARCHAR(50) NOT NULL,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "uk_sr_materials_material_code" UNIQUE ("material_code")
);

CREATE INDEX IF NOT EXISTS "idx_sr_materials_material_type" ON "sr_materials"("material_type");
CREATE INDEX IF NOT EXISTS "idx_sr_materials_status" ON "sr_materials"("status");

CREATE TABLE IF NOT EXISTS "sr_material_stock" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID,
    "project_id" UUID,
    "material_id" UUID NOT NULL,
    "available_quantity" NUMERIC(15,2) NOT NULL,
    "reserved_quantity" NUMERIC(15,2) DEFAULT 0,
    "unit" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_material_stock_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE SET NULL,
    CONSTRAINT "fk_sr_material_stock_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE SET NULL,
    CONSTRAINT "fk_sr_material_stock_material_id" FOREIGN KEY ("material_id") REFERENCES "sr_materials"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_material_stock_available_quantity" CHECK ("available_quantity" >= 0),
    CONSTRAINT "chk_sr_material_stock_reserved_quantity" CHECK ("reserved_quantity" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_material_stock_organization_id" ON "sr_material_stock"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_stock_project_id" ON "sr_material_stock"("project_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_stock_material_id" ON "sr_material_stock"("material_id");

CREATE TABLE IF NOT EXISTS "sr_material_usage" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "material_id" UUID NOT NULL,
    "road_segment_id" UUID,
    "quantity_used" NUMERIC(15,2) NOT NULL,
    "usage_date" DATE NOT NULL,
    "used_for" VARCHAR(255),
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_material_usage_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_material_usage_material_id" FOREIGN KEY ("material_id") REFERENCES "sr_materials"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_material_usage_road_segment_id" FOREIGN KEY ("road_segment_id") REFERENCES "sr_road_segments"("id") ON DELETE SET NULL,
    CONSTRAINT "chk_sr_material_usage_quantity_used" CHECK ("quantity_used" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_material_usage_project_date" ON "sr_material_usage"("project_id", "usage_date");
CREATE INDEX IF NOT EXISTS "idx_sr_material_usage_material_id" ON "sr_material_usage"("material_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_usage_road_segment_id" ON "sr_material_usage"("road_segment_id");

CREATE TABLE IF NOT EXISTS "sr_material_purchases" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "project_id" UUID,
    "supplier_id" UUID,
    "purchase_number" VARCHAR(50) NOT NULL,
    "purchase_date" DATE NOT NULL,
    "total_amount" NUMERIC(15,2) NOT NULL,
    "payment_status" VARCHAR(50) NOT NULL,
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_material_purchases_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_material_purchases_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE SET NULL,
    CONSTRAINT "fk_sr_material_purchases_supplier_id" FOREIGN KEY ("supplier_id") REFERENCES "sr_suppliers"("id") ON DELETE SET NULL,
    CONSTRAINT "uk_sr_material_purchases_org_purchase_number" UNIQUE ("organization_id", "purchase_number"),
    CONSTRAINT "chk_sr_material_purchases_total_amount" CHECK ("total_amount" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_material_purchases_organization_id" ON "sr_material_purchases"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_purchases_project_id" ON "sr_material_purchases"("project_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_purchases_supplier_id" ON "sr_material_purchases"("supplier_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_purchases_purchase_date" ON "sr_material_purchases"("purchase_date");

CREATE TABLE IF NOT EXISTS "sr_material_purchase_items" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "purchase_id" UUID NOT NULL,
    "material_id" UUID NOT NULL,
    "quantity" NUMERIC(15,2) NOT NULL,
    "unit" VARCHAR(50) NOT NULL,
    "unit_price" NUMERIC(15,2) NOT NULL,
    "total_price" NUMERIC(15,2) NOT NULL,
    
    CONSTRAINT "fk_sr_material_purchase_items_purchase_id" FOREIGN KEY ("purchase_id") REFERENCES "sr_material_purchases"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_material_purchase_items_material_id" FOREIGN KEY ("material_id") REFERENCES "sr_materials"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_material_purchase_items_quantity" CHECK ("quantity" >= 0),
    CONSTRAINT "chk_sr_material_purchase_items_unit_price" CHECK ("unit_price" >= 0),
    CONSTRAINT "chk_sr_material_purchase_items_total_price" CHECK ("total_price" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_material_purchase_items_purchase_id" ON "sr_material_purchase_items"("purchase_id");
CREATE INDEX IF NOT EXISTS "idx_sr_material_purchase_items_material_id" ON "sr_material_purchase_items"("material_id");

-- ============================================
-- 9. SUPPLIER MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_suppliers" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "supplier_code" VARCHAR(50) NOT NULL,
    "supplier_name" VARCHAR(255) NOT NULL,
    "supplier_type" VARCHAR(50),
    "phone_number" VARCHAR(20),
    "email_id" VARCHAR(255),
    "address" TEXT,
    "gst_number" VARCHAR(50),
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_suppliers_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_suppliers_org_supplier_code" UNIQUE ("organization_id", "supplier_code")
);

CREATE INDEX IF NOT EXISTS "idx_sr_suppliers_organization_id" ON "sr_suppliers"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_suppliers_supplier_type" ON "sr_suppliers"("supplier_type");
CREATE INDEX IF NOT EXISTS "idx_sr_suppliers_status" ON "sr_suppliers"("status");

-- ============================================
-- 10. EXPENSE MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_expense_categories" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "category_code" VARCHAR(50) NOT NULL,
    "category_name" VARCHAR(255) NOT NULL,
    "description" TEXT,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "uk_sr_expense_categories_category_code" UNIQUE ("category_code")
);

CREATE INDEX IF NOT EXISTS "idx_sr_expense_categories_status" ON "sr_expense_categories"("status");

CREATE TABLE IF NOT EXISTS "sr_expenses" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "project_id" UUID,
    "expense_category_id" UUID NOT NULL,
    "expense_date" DATE NOT NULL,
    "amount" NUMERIC(15,2) NOT NULL,
    "payment_mode" VARCHAR(50),
    "description" TEXT,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_expenses_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_expenses_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE SET NULL,
    CONSTRAINT "fk_sr_expenses_expense_category_id" FOREIGN KEY ("expense_category_id") REFERENCES "sr_expense_categories"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_expenses_amount" CHECK ("amount" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_expenses_project_date" ON "sr_expenses"("project_id", "expense_date");
CREATE INDEX IF NOT EXISTS "idx_sr_expenses_organization_id" ON "sr_expenses"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_expenses_expense_category_id" ON "sr_expenses"("expense_category_id");

-- ============================================
-- 11. DAILY WORK PROGRESS
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_daily_work_progress" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "project_id" UUID NOT NULL,
    "road_segment_id" UUID,
    "progress_date" DATE NOT NULL,
    "work_description" TEXT NOT NULL,
    "completed_percentage" NUMERIC(5,2),
    "work_quantity" NUMERIC(15,2),
    "unit" VARCHAR(50),
    "remarks" TEXT,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_daily_work_progress_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_daily_work_progress_road_segment_id" FOREIGN KEY ("road_segment_id") REFERENCES "sr_road_segments"("id") ON DELETE SET NULL,
    CONSTRAINT "chk_sr_daily_work_progress_completed_percentage" CHECK ("completed_percentage" IS NULL OR ("completed_percentage" >= 0 AND "completed_percentage" <= 100)),
    CONSTRAINT "chk_sr_daily_work_progress_work_quantity" CHECK ("work_quantity" IS NULL OR "work_quantity" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_daily_work_progress_project_date" ON "sr_daily_work_progress"("project_id", "progress_date");
CREATE INDEX IF NOT EXISTS "idx_sr_daily_work_progress_road_segment_id" ON "sr_daily_work_progress"("road_segment_id");

-- ============================================
-- 12. DOCUMENT MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_documents" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "entity_type" VARCHAR(50) NOT NULL,
    "entity_id" UUID NOT NULL,
    "file_name" VARCHAR(255) NOT NULL,
    "file_path" TEXT NOT NULL,
    "file_type" VARCHAR(100),
    "file_size" NUMERIC(15,2),
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_documents_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_documents_file_size" CHECK ("file_size" IS NULL OR "file_size" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_documents_organization_id" ON "sr_documents"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_documents_entity_type_id" ON "sr_documents"("entity_type", "entity_id");

-- ============================================
-- 13. VEHICLE MANAGEMENT
-- ============================================

CREATE TABLE IF NOT EXISTS "sr_vehicles" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "organization_id" UUID NOT NULL,
    "vehicle_code" VARCHAR(50) NOT NULL,
    "vehicle_name" VARCHAR(255) NOT NULL,
    "vehicle_type" VARCHAR(50) NOT NULL,
    "registration_number" VARCHAR(50),
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_vehicles_organization_id" FOREIGN KEY ("organization_id") REFERENCES "sr_organizations"("id") ON DELETE RESTRICT,
    CONSTRAINT "uk_sr_vehicles_org_vehicle_code" UNIQUE ("organization_id", "vehicle_code")
);

CREATE INDEX IF NOT EXISTS "idx_sr_vehicles_organization_id" ON "sr_vehicles"("organization_id");
CREATE INDEX IF NOT EXISTS "idx_sr_vehicles_vehicle_type" ON "sr_vehicles"("vehicle_type");
CREATE INDEX IF NOT EXISTS "idx_sr_vehicles_status" ON "sr_vehicles"("status");

CREATE TABLE IF NOT EXISTS "sr_vehicle_usage" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,
    
    "vehicle_id" UUID NOT NULL,
    "project_id" UUID NOT NULL,
    "usage_date" DATE NOT NULL,
    "start_km" NUMERIC(15,2),
    "end_km" NUMERIC(15,2),
    "purpose" VARCHAR(255),
    "remarks" TEXT,
    
    CONSTRAINT "fk_sr_vehicle_usage_vehicle_id" FOREIGN KEY ("vehicle_id") REFERENCES "sr_vehicles"("id") ON DELETE RESTRICT,
    CONSTRAINT "fk_sr_vehicle_usage_project_id" FOREIGN KEY ("project_id") REFERENCES "sr_projects"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_vehicle_usage_km" CHECK ("start_km" IS NULL OR "end_km" IS NULL OR "end_km" >= "start_km")
);

CREATE INDEX IF NOT EXISTS "idx_sr_vehicle_usage_vehicle_id" ON "sr_vehicle_usage"("vehicle_id");
CREATE INDEX IF NOT EXISTS "idx_sr_vehicle_usage_project_id" ON "sr_vehicle_usage"("project_id");
CREATE INDEX IF NOT EXISTS "idx_sr_vehicle_usage_usage_date" ON "sr_vehicle_usage"("usage_date");

CREATE TABLE IF NOT EXISTS "sr_vehicle_maintenance" (
    "id" UUID PRIMARY KEY NOT NULL DEFAULT gen_random_uuid(),
    "created_by" UUID NOT NULL,
    "date_created" TIMESTAMPTZ NOT NULL,
    "modified_by" UUID NOT NULL,
    "date_modified" TIMESTAMPTZ NOT NULL,
    "db_version" INT NOT NULL DEFAULT 1,

    "vehicle_id" UUID NOT NULL,
    "maintenance_type" VARCHAR(50) NOT NULL,
    "maintenance_date" DATE NOT NULL,
    "cost" NUMERIC(15,2),
    "description" TEXT,
    "next_maintenance_date" DATE,
    "status" VARCHAR(50) NOT NULL,
    
    CONSTRAINT "fk_sr_vehicle_maintenance_vehicle_id" FOREIGN KEY ("vehicle_id") REFERENCES "sr_vehicles"("id") ON DELETE RESTRICT,
    CONSTRAINT "chk_sr_vehicle_maintenance_cost" CHECK ("cost" IS NULL OR "cost" >= 0)
);

CREATE INDEX IF NOT EXISTS "idx_sr_vehicle_maintenance_vehicle_id" ON "sr_vehicle_maintenance"("vehicle_id");
CREATE INDEX IF NOT EXISTS "idx_sr_vehicle_maintenance_maintenance_date" ON "sr_vehicle_maintenance"("maintenance_date");

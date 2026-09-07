-- V15: Create Business Profile tables

-- Create sr_business_profiles table
CREATE TABLE sr_business_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(100),
    address_street TEXT,
    address_city VARCHAR(100),
    address_pin_code VARCHAR(10),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_business_profiles_organization_id ON sr_business_profiles(organization_id);

-- Create sr_business_contacts table
CREATE TABLE sr_business_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_profile_id UUID NOT NULL REFERENCES sr_business_profiles(id) ON DELETE CASCADE,
    contact_name VARCHAR(255) NOT NULL,
    contact_role VARCHAR(50) NOT NULL,
    phone_number_1 VARCHAR(20),
    phone_number_2 VARCHAR(20),
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_business_contacts_business_profile_id ON sr_business_contacts(business_profile_id);
CREATE UNIQUE INDEX idx_business_contacts_business_profile_role ON sr_business_contacts(business_profile_id, contact_role);

-- Create sr_business_services table
CREATE TABLE sr_business_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_profile_id UUID NOT NULL REFERENCES sr_business_profiles(id) ON DELETE CASCADE,
    service_name VARCHAR(255) NOT NULL,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_business_services_business_profile_id ON sr_business_services(business_profile_id);
CREATE UNIQUE INDEX idx_business_services_business_profile_name ON sr_business_services(business_profile_id, service_name);

-- V16: Create Company Profile tables (public brand identity, distinct from per-organization business profiles)

-- Defensive re-seed: the system user row inserted by V2 is referenced as the
-- created_by/modified_by audit value below (and throughout the codebase, e.g.
-- AuthService.SYSTEM_USER_ID). If it was ever removed outside of Flyway,
-- restore it here (idempotent, matches V2's insert exactly) so the FK
-- references in this migration don't fail.
INSERT INTO sr_users (
    id, created_by, date_created, modified_by, date_modified, db_version,
    country_code, email_id, phone_number, status, first_name, last_name,
    role, user_type, oauth_type, end_date, password, registration_source,
    email_verified_yn, oauth_signin_id
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    NOW(),
    '00000000-0000-0000-0000-000000000001',
    NOW(),
    1,
    NULL, 'system@nextenti.com', NULL, 'ACTIVE', 'System', 'User',
    'ADMIN', 'SYSTEM', NULL, NULL,
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
    'SYSTEM', TRUE, NULL
) ON CONFLICT (id) DO NOTHING;

-- Create sr_company_profiles table
CREATE TABLE sr_company_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    brand_name VARCHAR(255) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    business_type VARCHAR(100),
    product_name VARCHAR(100),
    tagline VARCHAR(255),
    phone VARCHAR(20),
    address TEXT,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

-- Create sr_company_contacts table
CREATE TABLE sr_company_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_profile_id UUID NOT NULL REFERENCES sr_company_profiles(id) ON DELETE CASCADE,
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

CREATE INDEX idx_company_contacts_company_profile_id ON sr_company_contacts(company_profile_id);
CREATE UNIQUE INDEX idx_company_contacts_company_profile_role ON sr_company_contacts(company_profile_id, contact_role);

-- Create sr_company_services table
CREATE TABLE sr_company_services (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_profile_id UUID NOT NULL REFERENCES sr_company_profiles(id) ON DELETE CASCADE,
    service_name VARCHAR(255) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX idx_company_services_company_profile_id ON sr_company_services(company_profile_id);
CREATE UNIQUE INDEX idx_company_services_company_profile_name ON sr_company_services(company_profile_id, service_name);

-- Seed the single public company profile record (fixed ID, referenced by CompanyProfileService)
INSERT INTO sr_company_profiles (
    id, brand_name, business_name, business_type, product_name, tagline, phone, address,
    created_by, date_created, modified_by, date_modified, db_version
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    'BHAGYAVANTI INFRA',
    'BhagyaVanti Developers & Civil Contractor',
    'Developers & Civil Contractor',
    'SMART ROAD',
    'BUILD ROADS • BUILD LIFE',
    '7676966391',
    'Opp. L.M.S. School, Old Jewargi Road, Panchashil Nagar, Gulbarga - 585102',
    '00000000-0000-0000-0000-000000000001', NOW(),
    '00000000-0000-0000-0000-000000000001', NOW(),
    1
) ON CONFLICT (id) DO NOTHING;

-- Seed the founder and additional contact
INSERT INTO sr_company_contacts (
    id, company_profile_id, contact_name, contact_role, phone_number_1,
    created_by, date_created, modified_by, date_modified, db_version
) VALUES
    ('20000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'Anand Rathod', 'MAIN_FOUNDER', NULL,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1),
    ('20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', 'Arvind Chavan', 'ADDITIONAL_CONTACT', NULL,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1)
ON CONFLICT (id) DO NOTHING;

-- Seed the six services, in display order
INSERT INTO sr_company_services (
    id, company_profile_id, service_name, display_order,
    created_by, date_created, modified_by, date_modified, db_version
) VALUES
    ('20000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'Hard Trimix', 1,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1),
    ('20000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000001', 'Flooring & Road Colour', 2,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1),
    ('20000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000001', 'Finishing', 3,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1),
    ('20000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000001', 'Groove Cutting', 4,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1),
    ('20000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000001', 'Dambar Filling', 5,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1),
    ('20000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000001', 'Chemical Filling', 6,
        '00000000-0000-0000-0000-000000000001', NOW(), '00000000-0000-0000-0000-000000000001', NOW(), 1)
ON CONFLICT (id) DO NOTHING;

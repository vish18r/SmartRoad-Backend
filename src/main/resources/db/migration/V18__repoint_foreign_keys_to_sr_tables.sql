-- Repoint foreign keys onto the tables the JPA entities actually use.
--
-- The entities map to sr_organizations / sr_projects / sr_clients, which Hibernate
-- created via ddl-auto=update, while the V1-V9 migrations created a parallel set of
-- organizations / projects / clients tables. Every FK below still referenced that
-- second, unused set, so inserting a worker or a material failed with
-- "violates foreign key constraint ... is not present in table organizations".
--
-- The legacy tables were empty when this migration was written; all live rows are in
-- the sr_* tables, so no data is moved or deleted here. The empty legacy tables are
-- deliberately left in place rather than dropped.

ALTER TABLE sr_business_profiles DROP CONSTRAINT IF EXISTS sr_business_profiles_organization_id_fkey;
ALTER TABLE sr_business_profiles ADD CONSTRAINT sr_business_profiles_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES sr_organizations(id) ON DELETE CASCADE;

ALTER TABLE sr_contracts DROP CONSTRAINT IF EXISTS sr_contracts_client_id_fkey;
ALTER TABLE sr_contracts ADD CONSTRAINT sr_contracts_client_id_fkey FOREIGN KEY (client_id) REFERENCES sr_clients(id);

ALTER TABLE sr_contracts DROP CONSTRAINT IF EXISTS sr_contracts_project_id_fkey;
ALTER TABLE sr_contracts ADD CONSTRAINT sr_contracts_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_daily_photo_timelines DROP CONSTRAINT IF EXISTS sr_daily_photo_timelines_project_id_fkey;
ALTER TABLE sr_daily_photo_timelines ADD CONSTRAINT sr_daily_photo_timelines_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_geofence_zones DROP CONSTRAINT IF EXISTS sr_geofence_zones_project_id_fkey;
ALTER TABLE sr_geofence_zones ADD CONSTRAINT sr_geofence_zones_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_gps_locations DROP CONSTRAINT IF EXISTS sr_gps_locations_project_id_fkey;
ALTER TABLE sr_gps_locations ADD CONSTRAINT sr_gps_locations_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_grn DROP CONSTRAINT IF EXISTS sr_grn_project_id_fkey;
ALTER TABLE sr_grn ADD CONSTRAINT sr_grn_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_machines DROP CONSTRAINT IF EXISTS sr_machines_current_site_id_fkey;
ALTER TABLE sr_machines ADD CONSTRAINT sr_machines_current_site_id_fkey FOREIGN KEY (current_site_id) REFERENCES sr_projects(id);

ALTER TABLE sr_machines DROP CONSTRAINT IF EXISTS sr_machines_organization_id_fkey;
ALTER TABLE sr_machines ADD CONSTRAINT sr_machines_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES sr_organizations(id) ON DELETE CASCADE;

ALTER TABLE sr_material_stock DROP CONSTRAINT IF EXISTS sr_material_stock_project_id_fkey;
ALTER TABLE sr_material_stock ADD CONSTRAINT sr_material_stock_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_material_stock_ledger DROP CONSTRAINT IF EXISTS sr_material_stock_ledger_project_id_fkey;
ALTER TABLE sr_material_stock_ledger ADD CONSTRAINT sr_material_stock_ledger_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_materials DROP CONSTRAINT IF EXISTS sr_materials_organization_id_fkey;
ALTER TABLE sr_materials ADD CONSTRAINT sr_materials_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES sr_organizations(id) ON DELETE CASCADE;

ALTER TABLE sr_purchase_orders DROP CONSTRAINT IF EXISTS sr_purchase_orders_project_id_fkey;
ALTER TABLE sr_purchase_orders ADD CONSTRAINT sr_purchase_orders_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_qr_codes DROP CONSTRAINT IF EXISTS sr_qr_codes_project_id_fkey;
ALTER TABLE sr_qr_codes ADD CONSTRAINT sr_qr_codes_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_qr_scans DROP CONSTRAINT IF EXISTS sr_qr_scans_project_id_fkey;
ALTER TABLE sr_qr_scans ADD CONSTRAINT sr_qr_scans_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_stock_transfers DROP CONSTRAINT IF EXISTS sr_stock_transfers_destination_project_id_fkey;
ALTER TABLE sr_stock_transfers ADD CONSTRAINT sr_stock_transfers_destination_project_id_fkey FOREIGN KEY (destination_project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_stock_transfers DROP CONSTRAINT IF EXISTS sr_stock_transfers_source_project_id_fkey;
ALTER TABLE sr_stock_transfers ADD CONSTRAINT sr_stock_transfers_source_project_id_fkey FOREIGN KEY (source_project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_vendors DROP CONSTRAINT IF EXISTS sr_vendors_organization_id_fkey;
ALTER TABLE sr_vendors ADD CONSTRAINT sr_vendors_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES sr_organizations(id) ON DELETE CASCADE;

ALTER TABLE sr_work_completion_certificates DROP CONSTRAINT IF EXISTS sr_work_completion_certificates_project_id_fkey;
ALTER TABLE sr_work_completion_certificates ADD CONSTRAINT sr_work_completion_certificates_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_work_photos DROP CONSTRAINT IF EXISTS sr_work_photos_project_id_fkey;
ALTER TABLE sr_work_photos ADD CONSTRAINT sr_work_photos_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_worker_attendance DROP CONSTRAINT IF EXISTS sr_worker_attendance_project_id_fkey;
ALTER TABLE sr_worker_attendance ADD CONSTRAINT sr_worker_attendance_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_worker_checkins DROP CONSTRAINT IF EXISTS sr_worker_checkins_project_id_fkey;
ALTER TABLE sr_worker_checkins ADD CONSTRAINT sr_worker_checkins_project_id_fkey FOREIGN KEY (project_id) REFERENCES sr_projects(id) ON DELETE CASCADE;

ALTER TABLE sr_workers DROP CONSTRAINT IF EXISTS sr_workers_assigned_site_id_fkey;
ALTER TABLE sr_workers ADD CONSTRAINT sr_workers_assigned_site_id_fkey FOREIGN KEY (assigned_site_id) REFERENCES sr_projects(id);

ALTER TABLE sr_workers DROP CONSTRAINT IF EXISTS sr_workers_organization_id_fkey;
ALTER TABLE sr_workers ADD CONSTRAINT sr_workers_organization_id_fkey FOREIGN KEY (organization_id) REFERENCES sr_organizations(id) ON DELETE CASCADE;

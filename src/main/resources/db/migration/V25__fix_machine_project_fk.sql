ALTER TABLE sr_machines DROP CONSTRAINT IF EXISTS sr_machines_current_site_id_fkey;
ALTER TABLE sr_machines ADD CONSTRAINT fk_sr_machines_current_site_id FOREIGN KEY (current_site_id) REFERENCES sr_projects(id);

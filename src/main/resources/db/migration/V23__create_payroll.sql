-- Worker wage configuration: added to sr_workers rather than a separate table since every
-- worker has at most one active wage configuration at a time (spec section 2).
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS wage_type VARCHAR(20) NOT NULL DEFAULT 'DAILY';
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS daily_wage NUMERIC(12, 2);
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS monthly_wage NUMERIC(12, 2);
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS overtime_rate NUMERIC(12, 2);
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS wage_effective_date DATE;
ALTER TABLE sr_workers ADD COLUMN IF NOT EXISTS wage_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE sr_workers ADD CONSTRAINT chk_sr_workers_daily_wage CHECK (daily_wage IS NULL OR daily_wage >= 0);
ALTER TABLE sr_workers ADD CONSTRAINT chk_sr_workers_monthly_wage CHECK (monthly_wage IS NULL OR monthly_wage >= 0);
ALTER TABLE sr_workers ADD CONSTRAINT chk_sr_workers_overtime_rate CHECK (overtime_rate IS NULL OR overtime_rate >= 0);

-- Payroll header: one row per worker, per calculated payroll period.
CREATE TABLE sr_payrolls (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    worker_id UUID NOT NULL REFERENCES sr_workers(id) ON DELETE CASCADE,
    employee_id VARCHAR(20) NOT NULL,
    payroll_start_date DATE NOT NULL,
    payroll_end_date DATE NOT NULL,
    wage_type VARCHAR(20) NOT NULL,
    daily_wage NUMERIC(12, 2),
    monthly_wage NUMERIC(12, 2),
    paid_days NUMERIC(6, 2) NOT NULL DEFAULT 0,
    absent_days NUMERIC(6, 2) NOT NULL DEFAULT 0,
    total_worked_hours NUMERIC(8, 2) NOT NULL DEFAULT 0,
    regular_hours NUMERIC(8, 2) NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(8, 2) NOT NULL DEFAULT 0,
    overtime_rate NUMERIC(12, 2) NOT NULL DEFAULT 0,
    basic_wage NUMERIC(14, 2) NOT NULL DEFAULT 0,
    overtime_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_allowances NUMERIC(14, 2) NOT NULL DEFAULT 0,
    gross_pay NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_deductions NUMERIC(14, 2) NOT NULL DEFAULT 0,
    net_pay NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    processed_at TIMESTAMP WITH TIME ZONE,
    approved_at TIMESTAMP WITH TIME ZONE,
    paid_at TIMESTAMP WITH TIME ZONE,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_payrolls_period CHECK (payroll_end_date >= payroll_start_date),
    CONSTRAINT chk_sr_payrolls_paid_days CHECK (paid_days >= 0),
    CONSTRAINT chk_sr_payrolls_absent_days CHECK (absent_days >= 0),
    -- Exact duplicate periods are rejected here; overlapping (non-identical) periods for the
    -- same worker are rejected by PayrollService at the application layer, where CANCELLED
    -- payrolls can be excluded from the overlap check.
    CONSTRAINT uq_sr_payrolls_worker_period UNIQUE (worker_id, payroll_start_date, payroll_end_date)
);

CREATE INDEX idx_sr_payrolls_organization_id ON sr_payrolls(organization_id);
CREATE INDEX idx_sr_payrolls_worker_id ON sr_payrolls(worker_id);
CREATE INDEX idx_sr_payrolls_status ON sr_payrolls(status);
CREATE INDEX idx_sr_payrolls_period ON sr_payrolls(payroll_start_date, payroll_end_date);

-- Itemized allowances for a payroll (Travel, Food, Accommodation, Site, Performance, Other).
CREATE TABLE sr_payroll_allowances (
    id UUID PRIMARY KEY,
    payroll_id UUID NOT NULL REFERENCES sr_payrolls(id) ON DELETE CASCADE,
    allowance_type VARCHAR(30) NOT NULL,
    description TEXT,
    amount NUMERIC(14, 2) NOT NULL,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_payroll_allowances_amount CHECK (amount >= 0)
);

CREATE INDEX idx_sr_payroll_allowances_payroll_id ON sr_payroll_allowances(payroll_id);

-- Itemized deductions for a payroll (Salary Advance, Loan, Late, Absence, Other).
CREATE TABLE sr_payroll_deductions (
    id UUID PRIMARY KEY,
    payroll_id UUID NOT NULL REFERENCES sr_payrolls(id) ON DELETE CASCADE,
    deduction_type VARCHAR(30) NOT NULL,
    description TEXT,
    amount NUMERIC(14, 2) NOT NULL,
    created_by UUID NOT NULL REFERENCES sr_users(id),
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by UUID NOT NULL REFERENCES sr_users(id),
    date_modified TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    db_version INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT chk_sr_payroll_deductions_amount CHECK (amount >= 0)
);

CREATE INDEX idx_sr_payroll_deductions_payroll_id ON sr_payroll_deductions(payroll_id);

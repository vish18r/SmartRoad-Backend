# Payroll Module

Calculates and processes worker wages for SMART ROAD (BHAGYAVANTI INFRA), built on the existing
worker and attendance modules. The backend is the sole source of truth for every monetary and
attendance-derived figure; the frontend only ever displays what the API returns.

## Architecture

```
PayrollController -> PayrollService -> PayrollCalculationService -> AttendanceService (reused, unchanged)
                                     -> PayrollRepository / PayrollAllowanceRepository / PayrollDeductionRepository
                                     -> WorkerService (reused, for worker lookup + authorization)
```

- `PayrollCalculationService` is the centralized calculation engine. It never recomputes worked
  hours itself — it calls `AttendanceService.summaryForWorker(...)`, which sums every closed
  check-in/check-out session per day (never `lastCheckOut - firstCheckIn`), and turns that into
  present/absent/paid days and regular/overtime hours.
- `PayrollService` owns persistence, validation and the DRAFT → PROCESSED → APPROVED → PAID →
  CANCELLED status workflow.
- `PayrollMapper` assembles the response DTO from the payroll entity (or a calculation preview),
  the worker, the worker's site name and the allowance/deduction line items. It is a plain
  `@Component`, not a MapStruct interface, because the response draws on entities that are never
  related by a JPA association.

## Database Tables (Flyway `V23__create_payroll.sql`)

- `sr_workers` gains a wage configuration: `wage_type` (DAILY/MONTHLY), `daily_wage`,
  `monthly_wage`, `overtime_rate`, `wage_effective_date`, `wage_active`. Set through the existing
  `POST /api/v1/workers` / `PUT /api/v1/workers/{id}` endpoints — no new worker endpoint was added.
- `sr_payrolls` — one row per worker per calculated payroll period: worker/employee identity,
  period, wage snapshot, attendance figures (paid/absent days, worked/regular/overtime hours),
  earnings (basic wage, overtime amount, total allowances, gross pay), total deductions, net pay,
  status, and `processed_at`/`approved_at`/`paid_at`. A unique constraint on
  `(worker_id, payroll_start_date, payroll_end_date)` blocks exact duplicates at the database
  level; overlapping-but-not-identical periods are blocked by `PayrollService` so a cancelled
  payroll can be excluded from the check.
- `sr_payroll_allowances` / `sr_payroll_deductions` — itemized lines per payroll
  (`allowance_type`/`deduction_type`, `description`, `amount`), each `NUMERIC(14,2)` and
  `CHECK (amount >= 0)`.

All monetary columns are `NUMERIC`; the Java side uses `BigDecimal` everywhere, rounded
`HALF_UP` to 2 decimal places at every figure returned to the client.

## Calculation Formula

```
Basic Wage   = dailyWage * paidDays                         (wageType = DAILY)
             = (monthlyWage / calendarDaysInPeriod) * paidDays  (wageType = MONTHLY)
Overtime Pay = overtimeHours * overtimeRate
Gross Pay    = Basic Wage + Overtime Pay + sum(allowances)
Net Pay      = Gross Pay - sum(deductions)
```

Per day, worked hours come from `AttendanceService` (sum of closed sessions that day):
- `regularHours(day) = min(workedHours(day), regularHoursPerDay)` (default 8, overridable per request)
- `overtimeHours(day) = max(0, workedHours(day) - regularHoursPerDay)`
- `paidDays(day)` = 1.0 if `workedHours(day) >= regularHoursPerDay`, otherwise rounded to the
  nearest 0.5 (a day with any closed session always earns at least a half day) — this is how
  half days are supported without a separate field.

A day with only an open (not checked-out) session contributes no worked hours and is not counted
present, since it has no measurable, payable duration.

## API Endpoints (base path `/api/v1/payroll`, all `hasRole('ADMIN')`)

| Method | Path | Purpose |
|---|---|---|
| POST | `/calculate` | Preview a calculation for a worker/period; nothing is persisted |
| POST | `` | Create and persist a payroll in DRAFT status |
| GET | `` | List payrolls, filtered by organization/worker/employeeId/status/wageType/period/search, paginated |
| GET | `/{id}` | Full payroll details |
| PUT | `/{id}` | Update a DRAFT payroll (recalculates) |
| POST | `/{id}/process` | DRAFT → PROCESSED |
| POST | `/{id}/approve` | PROCESSED → APPROVED |
| POST | `/{id}/mark-paid` | APPROVED → PAID |
| POST | `/{id}/cancel` | DRAFT/PROCESSED/APPROVED → CANCELLED |
| GET | `/worker/{workerId}` | A worker's full payroll history |
| GET | `/summary` | Dashboard aggregates (total workers, gross, deductions, net pay, pending/paid counts) |

## Status Flow

```
DRAFT --process--> PROCESSED --approve--> APPROVED --mark-paid--> PAID
  |                    |                      |
  +---------cancel-----+----------cancel------+
                                                     (PAID and CANCELLED are terminal)
```

Only a DRAFT payroll can be edited (`PUT`); recalculation only happens on create/update — once
processed, figures are locked and `process`/`approve`/`mark-paid` only transition status and stamp
a timestamp. `cancel` is rejected once a payroll is PAID or already CANCELLED.

## Wage Types, Allowances, Deductions

- `WageTypeEnum`: `DAILY`, `MONTHLY` (on the worker and snapshotted onto the payroll).
- `AllowanceTypeEnum`: `TRAVEL`, `FOOD`, `ACCOMMODATION`, `SITE`, `PERFORMANCE`, `OTHER`.
- `DeductionTypeEnum`: `SALARY_ADVANCE`, `LOAN_DEDUCTION`, `LATE_DEDUCTION`, `ABSENCE_DEDUCTION`,
  `OTHER_DEDUCTION`.
- `PayrollStatusEnum`: `DRAFT`, `PROCESSED`, `APPROVED`, `PAID`, `CANCELLED`.

## Assumptions

- Regular working hours per day default to 8 and can be overridden per calculation/payroll.
- A monthly wage is prorated by calendar days in the chosen period, not a fixed 30/31.
- The regular-hours-per-day setting used for a processed payroll is not separately persisted
  (only its effect — regular/overtime hours — is); reopening a PROCESSED/APPROVED/PAID payroll's
  exact per-day split is therefore not possible from the stored record alone.
- Worker active/deleted status is validated at calculate/create/update time, not re-checked on
  process/approve/mark-paid/cancel.

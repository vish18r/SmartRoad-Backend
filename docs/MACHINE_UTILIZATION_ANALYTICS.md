# Machine Utilization Analytics

Backend for the `/machine-utilization` analytics page. All KPIs are computed from real
`sr_machine_usage_sessions` rows (new table, this feature) joined with the existing
`sr_machines`, `sr_projects`, and `sr_resource_allocations`/`sr_resource_plans` data. No metric is
fabricated: any figure whose source data does not exist is returned as `null` with an
`*Available = false` flag and a `*UnavailableReason` string explaining exactly why.

## New table: `sr_machine_usage_sessions`

One row per tracked usage session for a machine (start → complete). Columns: `machine_id`,
`project_id`, `usage_date`, `start_time`/`end_time` (timestamptz, so a session may cross
midnight), `running_hours`, `idle_hours`, `operator_worker_id` (FK to `sr_workers`) /
`operator_name` (free text fallback), `status` (`UsageStatusEnum`), `source` (`UsageSourceEnum`),
`fuel_consumed`/`fuel_cost` (optional, per session), `notes`. Indexes: `(machine_id, usage_date)`,
`(project_id)`, `(start_time, end_time)`.

### `UsageStatusEnum`
`RUNNING`, `IDLE`, `SCHEDULED_UNUSED`, `MAINTENANCE_UNAVAILABLE`, `UNKNOWN` — chosen explicitly so
that unrecorded time is never silently counted as idle; a caller must say a machine was idle,
scheduled-but-unused, down for maintenance, or that its status for a window is simply unknown.

### `UsageSourceEnum`
`MANUAL` (operator/supervisor entry) or `DEVICE_LOG` (telematics/device feed) — there is currently
no device integration, so all sessions are created with `source = MANUAL` unless a future
integration supplies `DEVICE_LOG`.

## Overlap prevention (documented per the task's requirement)

Two mechanisms, deliberately redundant:

1. **Service-layer check (authoritative, works on both PostgreSQL and the H2 test database):**
   `MachineUsageSessionRepository.findOverlapping` runs a JPQL query for the same machine where
   `existing.start < candidate.end AND (existing.end IS NULL OR existing.end > candidate.start)`.
   `MachineUtilizationServiceImpl#assertNoOverlap` calls this before creating a session (candidate
   window = `[startTime, startTime]`, catching the case where another session is already open or
   contains that instant) and again before completing one (candidate window = the full
   `[start, end]`, excluding the session being completed).
2. **Database-level guard (PostgreSQL only, best-effort):** a partial unique index,
   `uq_sr_mus_one_open_session_per_machine ON sr_machine_usage_sessions(machine_id) WHERE end_time
   IS NULL AND is_deleted = FALSE`, prevents two simultaneously open sessions for the same machine
   even if the service check were ever bypassed. A full `EXCLUDE ... USING gist` constraint (via
   `btree_gist`) would also catch overlap between two already-completed sessions at the DB level,
   but it was intentionally **not** added because the test suite runs against H2, which does not
   support `EXCLUDE`/gist; the service-layer query is therefore the real validation for that case
   and is documented here instead of silently relying on a constraint that only works in one
   environment.

## Utilization formula

```
availableHours   = sum(runningHours) + sum(idleHours)   over sessions in the requested scope/period
utilizationPct    = runningHours / availableHours * 100   -- only when availableHours > 0
```

Because overlapping sessions for one machine are rejected at creation/completion time, summing
`runningHours`/`idleHours` across a machine's sessions cannot double-count time. `availableHours`
intentionally excludes `SCHEDULED_UNUSED`, `MAINTENANCE_UNAVAILABLE`, and `UNKNOWN` session time —
those categories are not "available running/idle time," they are different, explicitly distinct
states — so the percentage is never inflated or deflated by time nobody confirmed was available.
When `availableHours = 0` for the requested scope (no sessions recorded), `utilizationAvailable =
false` and the percentage is omitted rather than shown as `0%` or `null`-but-implied-zero.

## Operating cost per hour

```
operatingCostPerHour = totalFuelCost / totalRunningHours   -- only when fuel data exists AND runningHours > 0
```

Maintenance cost is **never** added into this figure today (see below), so the value currently
reflects fuel cost per running hour only; this is noted in the reason string whenever the
combined figure would otherwise mislead. If fuel is unavailable, or running hours are zero, the
whole figure is marked unavailable rather than silently divided by zero or treated as fuel-only
without explanation.

## Which metrics are real vs. unavailable

| Metric | Status | Reason when unavailable |
|---|---|---|
| Total machines | **Real** | `COUNT` over `sr_machines` |
| Machines used in period | **Real** | `COUNT(DISTINCT machine_id)` over sessions in range |
| Total running / idle hours | **Real** | `SUM` over sessions in range |
| Average utilization % | **Real when any hours recorded**, else unavailable | No sessions with running/idle hours recorded for the scope/period |
| Fuel consumed / fuel cost | **Real when at least one session recorded fuel data**, else unavailable | No fuel issuance/transaction table exists in this codebase; `MachineEntity` has only a static `fuelType` label, no transactional fuel records. Fuel is captured optionally per usage session going forward (`fuel_consumed`/`fuel_cost` on `sr_machine_usage_sessions`); until operators start entering it, the metric stays unavailable |
| Total maintenance cost | **Always unavailable** | `MachineEntity` stores only a current `maintenanceStatus` string (e.g. "scheduled"), not a cost field or any date-stamped history; there is no maintenance history table anywhere in the codebase. No trend/breakdown/total can be computed without fabricating data, so this metric is always returned as unavailable |
| Maintenance history (per-machine) | **Always unavailable** | Same reason as above |
| Operating cost per hour | **Real when fuel is available and running hours > 0**, else unavailable | Depends on fuel cost (see above) and/or would depend on maintenance cost (always unavailable) |
| Project-wise usage comparison | **Real** | Built from `sr_machine_usage_sessions.project_id` joined with `sr_projects`; machines with no `project_id` on their sessions are excluded from this specific breakdown |

## APIs

All under `@PreAuthorize("hasRole('ADMIN')")`, mirroring every existing controller in this repo
(`MachineController`, `ResourcePlanningController`), with the same `/api/v1/...` base path
convention and `organizationId` resolved from the caller when omitted
(`OrganizationService#resolveDefaultOrganizationId`).

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/machine-utilization/summary` | Org/project KPI summary for a date range |
| GET | `/api/v1/machine-utilization` | Paginated per-machine utilization list, filterable by project/machine/type/status/date range |
| GET | `/api/v1/machine-utilization/{machineId}` | Per-machine detail analytics (hours, utilization, fuel, maintenance, cost/hour, project split, usage history) |
| GET | `/api/v1/machine-utilization/projects` | Project-wise machine usage comparison |
| GET | `/api/v1/machines/{machineId}/usage` | Paginated raw usage session history for one machine |
| POST | `/api/v1/machine-utilization/sessions/start` | Starts a usage session |
| POST | `/api/v1/machine-utilization/sessions/{sessionId}/complete` | Completes an open usage session |

## Known gaps / follow-up work

- Fuel analytics remain unavailable until operators actually start recording `fuel_consumed`/
  `fuel_cost` on sessions, or until a dedicated fuel issuance/transaction table is added.
- Maintenance cost analytics will stay unavailable until a maintenance history table (with
  date-stamped cost records) is added in a future migration; today's `MachineEntity.maintenanceStatus`
  is a point-in-time label, not a cost or history source.
- `DEVICE_LOG` as a usage source is modeled but unused until a telematics/device integration exists.

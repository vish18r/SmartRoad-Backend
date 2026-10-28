# Maintenance Management

Backend for the `/maintenance` page (list + detail + calendar). Tracks preventive and corrective
maintenance for machines in a new `sr_maintenance_records` table (migration
`V29__create_maintenance_records.sql`). Machine details are never duplicated: a record references
`sr_machines` by `machine_id` and the API resolves machine name/type/registration at read time.

Code layout mirrors the Machine Utilization module:

| Layer | Location |
|---|---|
| Controller | `api/rest/maintenance/MaintenanceController.java` |
| Enums | `common/enums/maintenance/*` |
| DTOs | `core/dto/maintenance/*` |
| Mapper | `core/mapper/maintenance/MaintenanceMapper.java` (hand-written `@Component`, same convention as the sibling modules) |
| Service | `core/service/maintenance/MaintenanceService.java` + `MaintenanceServiceImpl.java` |
| Entity | `domain/entity/maintenance/MaintenanceRecordEntity.java` |
| Repository | `domain/repository/MaintenanceRecordRepository.java` |

## New table: `sr_maintenance_records`

Columns: `maintenance_reference` (unique, `MNT-000123`, drawn from the new `sr_maintenance_ref_seq`
sequence), `machine_id` (FK `sr_machines`, required), `project_id` (FK `sr_projects`, optional
project/site), `title`, `maintenance_type`, `category`, `description`, `priority`, `status`,
`scheduled_start` (required), `scheduled_end`, `due_by`, `actual_start`, `actual_end`,
`reported_at`, `technician_worker_id` (FK `sr_workers`, optional) / `technician_name` (free text
fallback - same pattern as the usage-session operator), `estimated_cost`, `actual_cost`,
`downtime_minutes`, `parts_used` (free text), `notes`, `cancellation_reason`, `recurrence_type`,
`parent_maintenance_id` (self FK, recurrence chain), `machine_label_before_start`, and the standard
audit columns (`created_by`, `modified_by`, `date_created`, `date_modified`, `db_version`) inherited
from `SmartRoadBaseEntity`. CHECK constraints enforce the enum value sets, `scheduled_end >
scheduled_start`, `actual_end >= actual_start`, and non-negative costs/downtime.

### Enums (`common/enums/maintenance`)

| Enum | Values |
|---|---|
| `MaintenanceTypeEnum` | `PREVENTIVE`, `CORRECTIVE` |
| `MaintenanceCategoryEnum` | Preventive: `SCHEDULED_SERVICING`, `OIL_FILTER_CHANGE`, `ROUTINE_INSPECTION`, `LUBRICATION`, `SCHEDULED_PART_REPLACEMENT`. Corrective: `BREAKDOWN`, `EQUIPMENT_REPAIR`, `FAULT_DIAGNOSIS`, `PART_REPLACEMENT`, `EMERGENCY_MAINTENANCE`. `OTHER` is valid for both. A category of the wrong type is rejected (400). |
| `MaintenancePriorityEnum` | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` (no shared priority enum existed in the codebase). For corrective work this is the reported severity. Defaults: `HIGH` for corrective, `MEDIUM` for preventive. |
| `MaintenanceStatusEnum` | `SCHEDULED`, `DUE`, `IN_PROGRESS`, `COMPLETED`, `OVERDUE`, `CANCELLED` |
| `MaintenanceRecurrenceEnum` | `ONE_TIME`, `DAILY`, `WEEKLY`, `MONTHLY` |
| `MaintenanceReminderTypeEnum` | `CREATED`, `DUE`, `OVERDUE`, `COMPLETED` |

As everywhere in this codebase, enums are stored UPPERCASE (`EnumType.STRING`) and serialized as
their lowercase `@JsonValue` (e.g. `"in_progress"`); inbound values are matched case-insensitively
by name or wire value.

## Status model and transitions

Only `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` are ever **persisted**. `DUE` and
`OVERDUE` are **derived on read** (`MaintenanceServiceImpl#deriveStatus`) because no scheduler
exists in this codebase (no `@EnableScheduling`, `@Scheduled`, or Quartz) that could flip a stored
flag. For a persisted `SCHEDULED` record:

```
due_by    = scheduled_end, or (when no end) the end of scheduled_start's calendar day in the
            offset the client supplied
OVERDUE   if due_by < now
DUE       else if scheduled_start < now + 24h      (DUE_WINDOW)
SCHEDULED otherwise
```

`due_by` is stored (computed at write time) so the derived statuses can be filtered/counted with
plain indexed JPQL range predicates; the repository filters built by `applyStatus` use exactly the
same boundaries as `deriveStatus`.

Server-enforced transitions (invalid ones return 400 `maintenance.invalid.status.transition`):

| Endpoint | From | To | Notes |
|---|---|---|---|
| `POST /{id}/start` | SCHEDULED (incl. derived DUE/OVERDUE) | IN_PROGRESS | sets `actual_start` (body value or now; rejected if > 5 min in the future); optional technician/notes |
| `POST /{id}/complete` | IN_PROGRESS | COMPLETED | requires `actual_start`; sets `actual_end` (body value or now; 400 if before start); computes downtime; optional actual cost / parts / notes |
| `POST /{id}/cancel` | SCHEDULED, DUE, OVERDUE, IN_PROGRESS | CANCELLED | optional reason |
| `DELETE /{id}` | anything but COMPLETED | CANCELLED | soft cancel, the row is kept - same convention as `DELETE /resource-planning/{id}`; deleting a COMPLETED record is 400 |

Edits (`PUT /{id}`):

- SCHEDULED/DUE/OVERDUE/IN_PROGRESS: all non-workflow fields are editable (status and actual
  times only change through the workflow endpoints). The machine of an IN_PROGRESS record cannot be
  changed.
- COMPLETED/CANCELLED: the record can never be reopened or rescheduled. Any change to a core field
  (machine, project, title, type, category, priority, recurrence, schedule, estimated cost,
  description, technician) is rejected with 400 `maintenance.not.editable`. Notes are always
  editable; actual cost and parts used are editable on COMPLETED records only (post-completion
  cost reconciliation).

Other validation: `scheduled_end` must be after `scheduled_start`; costs `>= 0` (Bean Validation
`@PositiveOrZero` plus DB CHECK); required `machineId`, `maintenanceType`, `title`,
`scheduledStart`; the machine must exist, not be soft-deleted, and belong to the organization
(404 `machine.not.found`); project and technician worker must exist when given.

## Preventive vs corrective workflow

- **Preventive**: created as SCHEDULED for a future slot, optionally recurring. Becomes DUE
  within 24h of its start and OVERDUE after its deadline; started and completed via the workflow
  endpoints.
- **Corrective / breakdown**: created the same way (the UI pre-fills `scheduledStart = now`),
  with `reportedAt` (defaults to now when omitted), severity via `priority` (defaults to HIGH),
  and the problem in `description`. Typically it is started immediately and completed when fixed;
  completion records `actual_end` and downtime exactly like preventive work. Corrective records
  cannot recur (400 `maintenance.recurrence.preventive.only`).

## Recurrence (chosen simple model)

Recurring preventive maintenance never materializes an unbounded series. At most **one pending
future occurrence** exists per chain:

1. Creating a recurring preventive record (`DAILY`/`WEEKLY`/`MONTHLY`) immediately generates the
   **next single occurrence** (same machine/title/category/priority/technician/estimate, schedule
   shifted by one interval - repeated only until it lands in the future), linked by
   `parent_maintenance_id`.
2. Completing a recurring record that does **not** yet have a generated successor generates its
   next occurrence. Because the record created in step 1 already has its successor, completing it
   generates nothing; completing that successor then generates the following one, and so on.
3. Cancelling an occurrence stops the chain (nothing new is generated); schedule a new recurring
   record to restart it.

## Downtime

`downtime_minutes = minutes between actual_start and actual_end`, computed server-side on
`/complete` from the two timestamps (timestamptz, so overnight/multi-day spans are correct). It is
never accepted from the client, and stays `null` when the timestamps are missing (records that
were never started/completed); no value is ever invented.

## Machine integration

- Creating a record for a non-existent, soft-deleted, or other-organization machine is rejected
  with 404 `machine.not.found`.
- `MachineEntity.maintenanceStatus` is an unconstrained free-text label (`VARCHAR(50)`) used by no
  logic anywhere in the codebase, so updating it is safe: `/start` sets it to `UNDER_MAINTENANCE`
  (remembering the previous label in `machine_label_before_start`); `/complete` and `/cancel` of an
  in-progress record restore the previous label, but only when no other record for the machine is
  still IN_PROGRESS and the label still reads `UNDER_MAINTENANCE` (i.e. nobody changed it manually
  meanwhile).
- **Out of scope / intentionally not touched:** `MachineEntity.status` (`MachineStatusEnum`, which
  has a `MAINTENANCE` value) and any availability flags. Changing the lifecycle status would affect
  resource-planning availability checks (`findByOrganizationIdAndStatusAndIsDeletedFalse(...,
  ACTIVE)`) and the machine utilization list, which is a behavioural change beyond this module.
- Wiring maintenance cost into Machine Utilization's `maintenanceCost*` fields is out of scope
  (those still report "unavailable"); `sr_maintenance_records.actual_cost` is now the data source a
  future change could use.

## Cost tracking

`estimated_cost` (planning) and `actual_cost` (recorded on completion or later reconciliation).
The summary's `totalCostInPeriod` is:

```
totalCostInPeriod = SUM(actual_cost   where actual_cost is recorded)
                  + SUM(estimated_cost where actual_cost is NULL)
-- over non-CANCELLED records whose scheduled_start falls in the period
```

Rationale: completed work should count at what it really cost, while scheduled/in-progress work
still contributes its estimate so the period total is not understated before invoices arrive. The
two halves are returned separately (`actualCostInPeriod`, `estimatedCostFallbackInPeriod`) so the
UI can show how much of the total is still an estimate. Cancelled records are excluded entirely.

## Summary metrics (`GET /maintenance/summary`)

- Snapshot (current state, independent of the period): `scheduled`, `dueSoon` (derived DUE),
  `overdue`, `inProgress`, `machinesUnderMaintenance` (distinct machines with an IN_PROGRESS
  record), `totalMachines` (used by the UI to disable "Schedule Maintenance" when there are no
  machines).
- Period (records whose `scheduled_start` is in `[startDate, endDate]`, default = current calendar
  month): `totalRecords`, `completed`, `cancelled`, `totalCostInPeriod`, `actualCostInPeriod`,
  `estimatedCostFallbackInPeriod`, `totalDowntimeMinutesInPeriod`.

Date-only parameters (`dateFrom`/`dateTo`, `startDate`/`endDate`) are interpreted as whole days in
the server's default time zone.

## Notifications / reminders

An existing `NotificationService` was found, but it only exposes `sendOtpViaSms` and
`sendOtpViaEmail` to an explicit phone/e-mail; there is no generic message API, no in-app
notification table, no recipient model for maintenance (who should be told), and no scheduler to
send time-based reminders. Wiring into it was therefore **not** trivial or safe, so:

- **Implemented:** `GET /api/v1/maintenance/reminders` (`MaintenanceService#getReminders`)
  computes, on demand, the reminders that would need to be sent right now: `dueReminders` (derived
  DUE) and `overdueReminders` (derived OVERDUE), each with a human-readable message (max 100 each).
- **Implemented:** created/completed events (including generated recurrence occurrences) are
  emitted as structured INFO log lines (`Maintenance notification event [CREATED|COMPLETED] ...`)
  from `MaintenanceServiceImpl#logNotificationEvent` - the single hook point for future dispatch.
- **Not implemented (no infrastructure):** periodic dispatch of due/overdue reminders. There is no
  `@EnableScheduling`/`@Scheduled`/Quartz anywhere in the codebase and none was invented. A future
  job (or an external cron calling `/maintenance/reminders`) plus a recipient/channel model is
  needed to actually send them.

## APIs

All under `@PreAuthorize("hasRole('ADMIN')")` with the `/api/v1/...` base path, mirroring
`MachineController`/`MachineUtilizationController`. `organizationId` is optional on every
organization-scoped endpoint and resolved from the caller when omitted
(`OrganizationService#resolveDefaultOrganizationId`); creation additionally requires organization
membership (`requireMember`, as `MachineService#create` does). Pagination uses `page` (1-based) and
`limit`, returning `{ items, pagination }` like the other modules.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/maintenance` | Create a record (and the next occurrence of a recurring preventive record) |
| GET | `/api/v1/maintenance` | Paginated list; filters `machineId`, `projectId`, `status` (effective, incl. due/overdue), `priority`, `type`, `dateFrom`, `dateTo` (scheduled start), `upcoming=true` (next 7 days), `overdue=true` |
| GET | `/api/v1/maintenance/{id}` | Record detail |
| PUT | `/api/v1/maintenance/{id}` | Update (terminal-record restrictions above) |
| DELETE | `/api/v1/maintenance/{id}` | Soft cancel (not allowed for COMPLETED) |
| POST | `/api/v1/maintenance/{id}/start` | SCHEDULED/DUE/OVERDUE to IN_PROGRESS (body optional) |
| POST | `/api/v1/maintenance/{id}/complete` | IN_PROGRESS to COMPLETED (body optional) |
| POST | `/api/v1/maintenance/{id}/cancel` | To CANCELLED (body optional) |
| GET | `/api/v1/machines/{machineId}/maintenance` | Paginated maintenance history of one machine |
| GET | `/api/v1/maintenance/upcoming` | Scheduled work starting in the next `days` (default 7, max 90) |
| GET | `/api/v1/maintenance/overdue` | Overdue work, oldest deadline first |
| GET | `/api/v1/maintenance/summary` | Dashboard metrics; `startDate`/`endDate` |
| GET | `/api/v1/maintenance/calendar` | All records scheduled in `startDate..endDate` (max 92 days) |
| GET | `/api/v1/maintenance/reminders` | Computed due/overdue reminders (nothing dispatched) |

Reused existing APIs: `GET /api/v1/machines` (machine picker / "no machines" check),
`GET /api/v1/projects`, `GET /api/v1/workers` (technician picker).

`GlobalExceptionHandler` additionally maps `MethodArgumentTypeMismatchException` (malformed UUID /
date query or path values) and `HttpMessageNotReadableException` (malformed JSON, unknown enum
value) to 400 instead of the generic 500.

## Known gaps / follow-up work

- No scheduled reminder dispatch (see Notifications).
- Machine lifecycle status / availability is not changed by maintenance (see Machine integration).
- Parts used is free text, not a structured parts/inventory link.
- Machine Utilization's maintenance-cost metrics are not yet wired to this table.

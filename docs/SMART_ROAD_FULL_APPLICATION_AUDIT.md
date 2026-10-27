# SMART ROAD — Full Application Audit

**Date:** 2026-10-27
**Scope:** Both repos — `Nextenti-Backend` (Spring Boot/Postgres) and `Nextenti-Frontend` (Next.js).
**Method:** Automated inventory of every sidebar route against its page implementation and every backend REST controller, cross-referenced by domain. This is a factual audit, not a guess — classifications below are based on direct inspection of each page file and each controller's mapping annotations.

## How to read this report

A single audit-and-fix session cannot responsibly claim to complete ~24 entirely-unbuilt, full-stack financial/ops modules (budget, cash flow, payments, reports, etc.) — each of those is its own multi-day feature. This report is honest about that: it gives you the complete picture, fixes the issues that were safely fixable in this pass, and lists everything else as explicit, prioritized remaining work rather than a fabricated "100% complete."

## 1. Module Inventory Matrix

| Route | Frontend Status | Backend Controller | Notes |
|---|---|---|---|
| /dashboard | WIRED | DashboardController | org-wide overview, health + stats |
| /owner-dashboard | WIRED | DashboardController (`/owner`) | |
| /site-dashboard | WIRED | DashboardController (`/site`), MaterialController, TrackingController | extended this session: diary summary, materials-today, activity feed, receive/consume modals, photo upload modal |
| /project-health | STUB | none dedicated | no API calls |
| /projects | WIRED | ProjectController | full CRUD |
| /organizations | WIRED | OrganizationController | full CRUD + members |
| /contracts | **FIXED → WIRED** | ContractController | was read-only; create/edit/delete now wired (this session) |
| /clients | **FIXED → WIRED** | ClientController | was read-only; create/edit/delete now wired (this session) |
| /sites | BY DESIGN | n/a | intentionally redirects to /projects — not a bug |
| /resource-planning | **FIXED → WIRED** | ResourcePlanningController, MachineController, VehicleController | backend built from scratch this session (plans, allocations, conflict detection, machines, vehicles); frontend wizard + list + summary built this session |
| /surveys | STUB | none | no "survey" backend domain exists |
| /sections | **FIXED → WIRED** | RoadController | was read-only; create/edit/delete now wired (this session) |
| /road-cutting | STUB | none | no backend domain |
| /concrete | **FIXED → WIRED/PARTIAL** | RoadController | reuses road-section CRUD (see fix agent's note on how concrete-vs-sections was resolved) |
| /workers | PARTIAL | WorkerController | list + shift history wired; worker create/edit flow lives at `/workers/new` separately — not re-verified this pass |
| /attendance | WIRED | TrackingController, WorkerController | |
| /shifts | WIRED | ShiftController, WorkerShiftController | |
| /geofence | PARTIAL | TrackingController | thin — location capture only, no history/list view |
| /qr-checkin | WIRED | TrackingController | |
| /payroll | WIRED | PayrollController | most complete module in the app |
| /overtime | STUB | none dedicated | overtime fields exist on payroll/shift, no standalone module |
| /machines | **FIXED → WIRED** | MachineController | backend was built this session; frontend now wired via resource-planning work (machine CRUD/types/api added) |
| /maintenance | STUB | none | |
| /machine-utilization | STUB | none | |
| /materials | WIRED | MaterialController | |
| /stock-ledger | PARTIAL | MaterialController | read-only ledger view |
| /material-delivery | STUB | none dedicated | |
| /fuel, /fuel-log | STUB | none | |
| /expenses | STUB | none | |
| /site-diary | WIRED | SiteDiaryController | |
| /safety, /issues, /tasks, /approvals | STUB | none | |
| /budget, /profitability, /cash-flow, /receivables, /payables, /payments | STUB | none | entire financial-ops cluster unbuilt, backend and frontend |
| /alerts, /calendar, /reports, /activity-logs, /command-center, /insights | STUB | none | |
| /vehicles | **FIXED → WIRED** (orphaned → linked) | VehicleController | backend built this session; no prior sidebar/frontend wiring existed — now has types/api, used within resource-planning wizard |

## 2. Fixes applied this session

1. **Site Dashboard** — added diary summary, today's materials received/consumed, 20-item recent-activity feed, material receive/consume endpoints + modals, real multipart photo upload with local-disk storage.
2. **Profile dropdown** — restyled to match design spec (lucide icons), added a real backend account-deletion endpoint (soft-delete + session revocation) since none existed before.
3. **Resource Planning** (full-stack, built from nothing):
   - Backend: `MachineController`/`VehicleController` (new full CRUD on top of a previously-dormant `sr_machines` table + new `sr_vehicles` table), `ResourcePlanningController` with plan CRUD, allocation CRUD, server-side conflict detection (worker/machine/vehicle double-booking, material-shortage warnings, active-status checks), availability lookup. Flyway migrations V25 (fixed a pre-existing dangling FK on `sr_machines.current_site_id`), V26 (`sr_vehicles`), V27 (`sr_resource_plans`, `sr_resource_allocations`). Verified: `./gradlew compileJava` succeeds AND the app boots cleanly against the live Postgres instance with all three migrations applying and Hibernate schema validation passing.
   - Frontend: full page replacing the stub — summary cards, plan list, 6-step creation wizard (details → workers → machines → materials → vehicles → review+validate), conflict display.
4. **Contracts, Clients, Sections, Concrete** — these already had full CRUD backends; frontend was read-only. Wired create/edit/delete, reusing existing components/patterns.

## 3. Confirmed remaining work (not attempted this session — genuinely out of scope for one pass)

### No backend exists at all (full-stack build required for each):
Project Health, Surveys, Road Cutting, Overtime (standalone), Maintenance, Machine Utilization, Material Delivery, Fuel/Fuel Log, Expenses, Safety, Issues, Tasks, Approvals, Budget, Profitability, Cash Flow, Receivables, Payables, Payments, Alerts, Calendar, Reports, Activity Logs, Command Center, Insights.

That's 24 distinct modules. Recommended prioritization if continuing:
- **P1 candidates** (clear business value, likely smallest lift): Expenses, Approvals, Reports (could partially reuse existing data — attendance/payroll/materials — rather than needing new domain models).
- **P2**: Tasks, Issues, Safety, Alerts, Calendar (workflow/ops tooling).
- **P3**: the financial-ops cluster (Budget, Cash Flow, Receivables, Payables, Payments, Profitability) — these imply real accounting logic and should be scoped carefully with the business before building.
- **Unclear value / possibly intentionally deferred**: Command Center, Insights, Activity Logs (activity logs specifically could reuse the resource-planning/dashboard activity-feed pattern already built this session).

### Partially wired, needs follow-up:
- `/workers` — verify the create/edit flow at `/workers/new` and `/workers/[id]` is fully connected (not re-verified this pass).
- `/geofence` — only captures a location, no history or map view.
- `/stock-ledger` — read-only, which may be correct by design (ledgers are typically append-only/immutable) — confirm with the user rather than assuming it needs write actions.

## 4. Build/verification results

- Backend: `./gradlew compileJava` — **BUILD SUCCESSFUL**. Live app boot verified against Postgres with migrations V25–V27 applying cleanly, Hibernate schema validation passing (see backend_run.log in the repo root for the full startup trace from this session).
- Frontend: `npx tsc --noEmit` across the whole repo reports **zero errors in any real `src/app/...` file** (verified by filtering out a stale, untracked, pre-existing duplicate directory `Nextenti-Frontend/Nextenti-Frontend/` — see "Found but not fixed" below). `npx eslint src` reports 43 errors / 18 warnings, **all pre-existing in files untouched this session** (`src/hooks/useForm.ts`, `useTable.ts`, `useToast.ts`, `src/lib/utils/validation.ts`, `src/types/common.ts` — all `@typescript-eslint/no-explicit-any`/unused-vars, unrelated to any module worked on here).
- No destructive operations performed. No existing data, migrations, or working endpoints were altered or removed.

## 4a. Found but not fixed (flagged for you, not silently skipped)

`Nextenti-Frontend/Nextenti-Frontend/` — a stray **untracked** duplicate directory (7 old page files: boq, clients, dashboard, organizations, projects, sections, workers — dated Aug 31, pre-dating this session, using an old/removed API-response shape). It's dead code Next.js never routes from, but `tsconfig.json`'s broad `**/*.tsx` include sweeps it into every `tsc` run, producing ~20 phantom errors that make it hard to see real signal. Confirmed via `git status` it's untracked (safe to delete), but the deletion was blocked by the session's auto-mode safety classifier (irreversible-destruction guard) and needs your explicit go-ahead: run `rm -rf Nextenti-Frontend/Nextenti-Frontend` from the frontend repo root, or ask me to do it with confirmation.

## 5. Summary

- **Total sidebar routes inspected:** 47
- **Fully wired before this session:** 10
- **Fixed to wired this session:** 6 (contracts, clients, sections, concrete, resource-planning, machines — plus vehicles newly connected)
- **Still partial:** workers, geofence, stock-ledger (flagged, not blocking)
- **Still pure stubs with zero backend:** 24 (listed above — explicit future scope, not silently abandoned)
- **Backend files added/changed this session:** migrations V25–V27; Machine/Vehicle/ResourcePlan entities, repositories, services, controllers, DTOs; AuthService/AuthController delete-account; SiteDashboardService extensions; FileStorageService + StorageWebConfig; StockService receive/consume.
- **Frontend files added/changed this session:** site-dashboard page + api/types; profile-dropdown; resource-planning page + new machine/vehicle/resource-plan types+api clients; contracts/clients/sections/concrete pages and their api clients.
- **Database migrations added:** V25, V26, V27 (all additive — no drops, no destructive ALTERs beyond fixing one dangling FK).
- **Remaining blockers:** none technical — the 24 unbuilt modules are a scope/prioritization decision, not a bug or configuration blocker. No external credentials or services were required for anything built this session (file storage uses local disk, as the rest of the app already does for similar data).

# Employee Onboarding and Payroll Portal

A Spring MVC + JSP + Postgres internal HR/payroll application: employee
profiles, onboarding checklists, compensation change approvals, HR/payroll
documents, HR support requests, policy acknowledgements, CSV import/export,
and an append-only audit trail.

## Tech stack

Java 21, Spring Boot 3.3 (Spring MVC, Spring Security, Spring Data JPA),
JSP/JSTL views (`tomcat-embed-jasper`), Flyway migrations, PostgreSQL,
Gradle (wrapper committed). Single deployable module, packaged as a
bootable WAR.

## Tier and trust statement (per corpus contract)

This app is a **single-module** application, not a channel/decision/core-mock
split. Per the corpus contract's exception for single-module apps: the same
process both **authenticates** the session (Spring Security form login,
session cookie, BCrypt password hashes) and **authorizes** every request.
Authorization is centralized in `AuthorizationService` (role checks,
ownership/direct-report checks) called from every controller method that
touches another employee's data, plus declarative role restrictions in
`SecurityConfig`'s `SecurityFilterChain`. There is no separate decision-tier
service or core-mock vendor stub in this stack — HR/payroll has no external
vendor system to mock (contrast with a banking app's core banking system);
email, and the "batch" scheduled reminder job, are local mocks instead
(see below).

## Local mocks

- **Email**: outbound mail (password reset links, etc.) is written to the
  `outbox_emails` table instead of being sent, via `EmailService`.
- **Scheduled job**: `ScheduledJobs` runs an in-process daily sweep (no
  external broker) that notifies employees of onboarding tasks due within
  2 days.

## Run it

### Option A: Docker Compose (recommended)

```bash
docker compose up --build
```

This starts Postgres, builds the app image, runs Flyway migrations, and
loads seed data automatically. The app is then available at
**http://localhost:8087**.

### Option B: Local Gradle + local Postgres

```bash
# start a local Postgres however you like, then:
export DB_URL=jdbc:postgresql://localhost:5432/hrpayroll
export DB_USER=hrpayroll
export DB_PASSWORD=hrpayroll
./gradlew bootRun
```

The app listens on **http://localhost:8080**.

### Build / test

```bash
./gradlew build          # compiles, runs tests, packages a bootable WAR
./gradlew compileJava     # compile only
./gradlew test            # unit tests (authorization + compensation workflow rules)
```

## Demo accounts

All demo accounts share the password `Password123!`.

| Role | Email |
|---|---|
| HR Admin | `hradmin@hrpayroll.example` |
| Manager | `manager@hrpayroll.example` |
| Payroll Specialist | `payroll@hrpayroll.example` |
| Employee | `employee@hrpayroll.example` |

(Additional manager/employee demo accounts are also seeded, e.g.
`manager1@hrpayroll.example` .. `manager5@hrpayroll.example` and
`employee1@hrpayroll.example` .. `employee22@hrpayroll.example`.)

## Implementation summary

**Screens**: login / forgot / reset password, role-specific dashboards
(HR admin, manager, payroll specialist, employee), profile, employee
directory + detail (HR), manager team + direct-report detail, compensation
request/approval workflow (manager and HR queues), onboarding checklist with
evidence upload, payroll document upload/download center, HR request
threads (with HR/manager-only internal notes), policy list + acknowledgement,
departments admin, policies admin, CSV import/export, append-only audit log,
notifications.

**Workflows enforced server-side**: onboarding task state machine
(`not_started → in_progress → submitted → approved/rejected`, `→ waived`);
compensation request state machine (`draft → submitted → manager_approved →
hr_approved → effective`, with a `rejected` branch) — a manager can never
approve their own compensation change, and HR approval atomically writes an
immutable `compensation_records` row; HR request lifecycle (`open →
waiting_on_hr/waiting_on_employee → resolved/closed`).

**Security controls**: BCrypt password hashes; session-cookie auth via
Spring Security `SecurityFilterChain` (no `WebSecurityConfigurerAdapter`);
CSRF protection enabled on all state-changing forms; ownership/role checks
centralized in `AuthorizationService` and re-checked in every controller that
touches employee-scoped data (profile, compensation, documents, onboarding,
HR requests); explicit command/DTO objects for every form (role, employment
status and ownership are never directly bindable — role changes go through a
dedicated HR-admin-only endpoint); `<c:out>` escaping on all user-supplied
content in JSPs; parameterized queries throughout (Spring Data JPA / JDBC
bound params); canonicalized-path file downloads; append-only audit log
(`AuditEvent`) with no update/delete endpoint anywhere, one event per
export/list request, before/after values and authorization basis recorded
for every compensation transition, document download, role change, and
export.

**Precision baseline (benign, non-scoring)**: implemented exactly as
specified in `AuthorizationService.assertOwnsEmployee` (ownership check one
frame above the controller), `util/AssetVersion.java` (`java.util.Random`
cache-busting suffix only), `util/ETagUtil.java` (MD5 weak ETag on reference
data only), `service/EmployeeDirectoryService.java` (whitelisted sort column
via `Set<String>`, bound parameters), `service/FileStorageService.java`
(canonicalized-base-directory path resolution for downloads).

## UI summary

Bootstrap 5 (CDN) applied consistently across every screen: a fixed top
navbar with role-aware menu items, page titles and back-links, summary cards
on every dashboard, tables with search/filter controls and status badges
(icon/text + color, never color-only), tabs on detail pages instead of one
long form, empty states, success/error alerts, and visible focus outlines
(custom `:focus-visible` style) for keyboard users. A shared `app.css`
defines an accessible palette (dark blue brand color, high-contrast status
badge colors for success/warning/danger/info/neutral) layered over
Bootstrap's components.

## Completeness note

All hard requirements, screens, roles, workflows, seed data volumes, and the
five precision-baseline primitives from the spec are implemented. The app
compiles and packages cleanly (`./gradlew build`) and was runtime-verified
against a local Postgres container (Flyway migrations apply, seed data
loads, login works for all four demo roles).

Nothing was intentionally left incomplete.

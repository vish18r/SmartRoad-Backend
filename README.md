# Nextenti-Backend

Backend for **SmartRoad** — a Road Construction & Contractor Management System.

Architecture and coding conventions mirror the existing `java_Nextenti` project (package-by-layer with
per-domain subpackages, `.properties`-based config, `NextentiException`-driven error handling). See
"Architecture" below for what was reused.

## Tech Stack

- Java 24
- Spring Boot 3.5
- Gradle
- PostgreSQL
- Spring Data JPA / Hibernate
- Spring Security + JWT
- Flyway
- Lombok
- Jakarta Validation
- Docker

## Getting Started

1. Copy `.env.example` to `.env` and fill in real values (a working `.env` with local defaults is already
   present for development).
2. Start PostgreSQL:
   ```
   docker compose up -d
   ```
3. Run the application (dev profile is active by default):
   ```
   ./gradlew bootRun
   ```
4. Verify the health endpoint:
   ```
   GET http://localhost:8080/api/v1/health
   ```

## Project Structure

```
src/main/java/com/nextenti/services
├── api
│   ├── error         # GlobalExceptionHandler (@RestControllerAdvice)
│   ├── interceptor    # Request-scoped interceptors (correlation id)
│   ├── rest           # Controllers, one subpackage per domain
│   └── utils           # HTTP/API-layer helpers
├── app
│   ├── audit           # Auditing wiring (AuditorAware, etc.) - added when needed
│   ├── config           # Security, JWT, and other @Configuration classes
│   └── web               # Property-source layering, WebMvcConfigurer
├── common
│   ├── enums            # Shared enum contracts
│   ├── exception          # NextentiException + subtypes
│   ├── util                # Domain-agnostic shared utilities
│   └── validation           # Custom Jakarta validation, when needed
├── core
│   ├── dto                  # Request/response DTOs, one subpackage per domain
│   ├── event                  # Domain events
│   ├── mapper                   # Entity <-> DTO mappers
│   └── service                    # Business logic, one subpackage per domain
├── domain
│   ├── entity                       # JPA entities (extend NextentiBaseEntity)
│   └── repository                     # Spring Data JPA repositories
├── scheduler                            # Scheduled/Quartz jobs (placeholder)
└── BackendApplication.java
```

## Architecture — reused from `java_Nextenti`

- Package-by-layer (`api` / `app` / `common` / `core` / `domain`) with a subpackage per business domain,
  rather than a flat `controller/service/repository/entity` layout.
- `.properties`-based configuration layered via `@PropertySources` (`application.properties`,
  `messages.properties`, `dbConfig.properties`) in `app/web/NextentiAppConfig`, instead of YAML.
- A single checked `NextentiException` (layer + error code + message key), with thin subclasses
  (`ResourceNotFoundException`, `BadRequestException`) for common cases, handled centrally in
  `api/error/GlobalExceptionHandler`.
- `NextentiBaseEntity` as a `@MappedSuperclass` with UUID id and JPA-audited `createdAt`/`updatedAt`.
- Explicit constructor injection everywhere — no field injection, no `@RequiredArgsConstructor`.
- JWT + Spring Security wired in `app/config` (`SecurityConfiguration`, `JwtAuthenticationFilter`,
  `ApplicationConfiguration`), with `JwtService`/`CustomUserDetailsService` under `core/service/auth`
  as business services rather than infra classes.
- Correlation-id request interceptor feeding the logging pattern (`RequestHeaderInterceptor`).
- Four environment-specific Dockerfiles (`Dockerfile-Dev/QA/UAT/Prod`), same multi-stage Gradle build
  pattern, differing only by `SPRING_PROFILES_ACTIVE`.

## Deliberate differences from `java_Nextenti`

- **API response shape**: uses a generic `NextentiApiResponse<T>` (`{success, message, data}`) instead of
  `java_Nextenti`'s `NextentiEntityResponse`/`ResponseEntityUtil` pattern, to match SmartRoad's required
  API contract.
- **Base entity**: kept minimal (`id`, `createdAt`, `updatedAt`) rather than `java_Nextenti`'s full
  `createdBy`/`modifiedBy`/`dbVersion` shape, since there's no User module yet to attribute audit fields to.
- **Database config**: all credentials are resolved from environment variables (`DB_HOST`, `DB_PORT`, ...)
  via placeholders in `dbConfig.properties`, rather than literal values in a gitignored file — nothing is
  hardcoded.
- **Flyway**: `java_Nextenti` doesn't use Flyway; it's added fresh here per SmartRoad's requirements,
  following `java_Nextenti`'s snake_case/UUID schema conventions.
- Business-specific `java_Nextenti` infrastructure (Quartz scheduling, MapStruct, OAuth/Firebase, AWS
  Secrets Manager, Loki logging, actuator, etc.) was intentionally **not** copied — only added when an
  actual module needs it.

## Notes

- Only `/api/v1/health` is public; all other endpoints require authentication once user management is
  implemented.
- Business modules (User, Project, Worker, Attendance, Machine, Material, Expense, Fuel, Daily Progress,
  Reports) are intentionally not implemented yet — this repo only contains the base setup.

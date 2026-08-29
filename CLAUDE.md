# Nextenti JavaApp — Claude Code Instructions

## Project Overview
Spring Boot 3.5.16 REST API for the Nextenti platform.  
**Java:** 24 | **DB:** PostgreSQL + pgvector | **Auth:** JWT (stateless) | **Build:** Gradle  
**Base package:** `com.nextenti.services` | **Context path:** `/nextenti` 

---

## Package Structure

```
com.nextenti.services/
├── api/
│   ├── rest/{domain}/          ← Controllers
│   ├── error/                  ← ErrorDetails
│   ├── interceptor/            ← RequestHeaderInterceptor
│   └── utils/                  ← ResponseEntityUtil, LogRequestTime AOP annotation, RequestUtil
├── app/
│   ├── config/                 ← SecurityConfiguration, ApplicationConfiguration, FirebaseConfig, MailConfig, etc.
│   ├── audit/                  ← AuditorAwareImpl
│   └── web/                    ← NextentiAppConfig
├── core/
│   ├── service/{domain}/       ← Business logic services (incl. service/integration/ for third-party API clients)
│   ├── dto/{domain}/           ← Data Transfer Objects (incl. dto/integration/ for third-party payloads)
│   └── mapper/{domain}/        ← MapStruct mappers
├── domain/
│   ├── entity/{domain}/        ← JPA entities (extend NextentiBaseEntity)
│   └── repository/{domain}/    ← Spring Data JPA repositories
└── common/
├── enums/                  ← All enums (CountryCodeEnum, GenderEnum, TimeZoneEnum, etc.); auth/, otp/, ntconfiguration/, userprofile/, jobs/, jobworkflow/, and jobtracker/ subpackages for domain-specific enums
├── exception/              ← NextentiException, GlobalExceptionHandler, ErrorCodeMapping
├── util/                   ← Shared utilities (DateTimeUtil, ValidationUtil, etc.)
└── validation/             ← Custom validators
```

**Existing domains:** `auth`, `userprofile`, `availability`, `jobs`, `jobworkflow`, `jobtracker`, `notification`, `ntconfiguration` 

---

## Architecture Rules

1. **Controller → Service → Repository** — never skip or cross layers.
2. Controllers handle HTTP only — zero business logic allowed.
3. Services own all business logic; throw `NextentiException` for every error case.
4. Repositories hold only database queries — no logic whatsoever.
5. Always use DTOs in API responses — never expose Entity classes directly in responses.
6. MapStruct Mappers convert Entity ↔ DTO — never map manually in service/controller code.

---

## Naming Conventions

| Class type   | Suffix/Pattern  | Example                   |
|--------------|-----------------|---------------------------|
| Controller   | `*Controller`   | `CandidateController`     |
| Service      | `*Service`      | `CandidateService`        |
| Repository   | `*Repository`   | `CandidateRepository`     |
| Entity       | `*Entity`       | `CandidateEntity`         |
| Mapper       | `*Mapper`       | `CandidateMapper`         |
| Enum         | `*Enum`         | `UserStatusEnum`          |
| Request DTO  | `*RequestDTO`   | `JobTemplateRequestDTO`   |
| Response DTO | `*ResponseDTO`  | `JobTemplateResponseDTO`  |
| Test         | `*Test`         | `CandidateControllerTest` |

---

## Base Classes — Always Extend

### Entity
`SrBaseEntity` provides: `id` (UUID, auto-generated), `createdBy`, `modifiedBy`, `dateCreated`, `dateModified`, `dbVersion` (@Version for optimistic locking). The base class already has `@EntityListeners(AuditingEntityListener.class)` — **do not repeat it on subclasses**.

```java
@Entity
@Table(name = "sr_<plural_table_name>")  // table names are always plural, e.g. sr_users
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleEntity extends SrBaseEntity {
    // Only define domain-specific fields here
}
```

### DTO
Base DTO provides: `id`, `createdBy`, `modifiedBy`, `createdDate`, `modifiedDate`, `dbVersion`.  
It is annotated with `@JsonInclude(JsonInclude.Include.NON_NULL)` — null fields are **automatically excluded** from JSON responses.  
`dbVersion` is annotated `@JsonIgnore` — it is **never serialized** to the client.

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExampleResponseDTO extends BaseDTO {
    // Only define domain-specific fields here
}
```

### Java records as request DTOs
Simple inbound-only request DTOs (no base class needed) may be written as Java records:

```java
public record ExampleRequestDTO(
    @NotBlank(message = "{field.required}")
    String fieldName
) {}
```

Use a record when the DTO is purely inbound (no base DTO fields needed) and immutable. Use a regular `@Data` class when the DTO extends base DTO or requires mutable fields.

---

## Controller Pattern

```java
@RestController
@RequestMapping("/example")
@SecurityRequirement(name = "bearerAuth")  // required for Swagger auth — omit only for /auth/**, /social/**, or /customers/** endpoints
public class ExampleController {

    private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);

    private final ExampleService exampleService;

    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    // GET single — return DTO directly in body
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> getExample(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside getExample method--");

        ExampleResponseDTO response = exampleService.getExample(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // GET list — return page/list DTO directly in body
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> getAllExamples(@RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside getAllExamples method--");

        ExamplePageResponseDTO response = exampleService.getAllExamples();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // POST — service returns EntityResponse; use HttpStatus.CREATED
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> createExample(@RequestBody @Valid ExampleRequestDTO dto,
                                                @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside createExample method--");

        EntityResponse response = exampleService.createExample(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT — service returns EntityResponse; use HttpStatus.OK
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> updateExample(@PathVariable UUID id,
                                                @RequestBody @Valid ExampleRequestDTO dto,
                                                @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside updateExample method--");

        EntityResponse response = exampleService.updateExample(id, dto);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // DELETE — service returns EntityResponse; use HttpStatus.OK
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> deleteExample(@PathVariable UUID id,
                                                @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside deleteExample method--");

        EntityResponse response = exampleService.deleteExample(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
```

**Rules:**
- Always declare `throws NextentiException` on every endpoint method.
- Always add `logger.info("--Inside <methodName> method--")` as the first line of each method.
- Always add `@SecurityRequirement(name = "bearerAuth")` at class level for Swagger (omit only on `/auth/**`, `/social/**`, or `/customers/**` controllers).
- Always add `@PreAuthorize("hasRole('ADMIN')")` on every endpoint method.
- Always include `@RequestHeader HttpHeaders headers` as the last parameter on every endpoint method.
- Use `UUID` for `@PathVariable` — never `String` for ID parameters.
- Always include `produces = MediaType.APPLICATION_JSON_VALUE` on every mapping. Add `consumes` only on POST/PUT/PATCH (use `MULTIPART_FORM_DATA_VALUE` for file-upload endpoints).
- Always place `@RequestBody` before `@Valid` on request body parameters.
- GET endpoints assign the service result to a `response` variable, then return `ResponseEntity.status(HttpStatus.OK).body(response)` — the DTO directly.
- POST returns `HttpStatus.CREATED`; all other methods (GET, PUT, PATCH, DELETE) return `HttpStatus.OK`.
- POST/PUT/PATCH/DELETE: the service returns `EntityResponse` (built via `ResponseEntityUtil`); the controller wraps it with `ResponseEntity.status(...).body(response)`.
- Use explicit constructor injection — not `@RequiredArgsConstructor`.

---

## Service Pattern

```java
@Service
public class ExampleService {

    private final ExampleRepository exampleRepository;
    private final ExampleMapper exampleMapper;

    public ExampleService(ExampleRepository exampleRepository, ExampleMapper exampleMapper) {
        this.exampleRepository = exampleRepository;
        this.exampleMapper = exampleMapper;
    }

    public ExampleResponseDTO getExample(String id) throws NextentiException {
        ExampleEntity entity = exampleRepository.findById(id)
            .orElseThrow(() -> new NextentiException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "example.not.found"
            ));
        return exampleMapper.toDTO(entity);
    }

    public ExampleResponseDTO createExample(ExampleRequestDTO dto) throws NextentiException {
        ExampleEntity entity = exampleMapper.toEntity(dto);
        return exampleMapper.toDTO(exampleRepository.save(entity));
    }
}
```

Multi-entity create flows that must be atomic (e.g. mint an auth user **and** persist a dependent profile in one request) annotate the service method with `@Transactional` so any failure rolls back the whole request and leaves no orphaned rows.

---

## Repository Pattern

```java
public interface ExampleRepository extends JpaRepository<ExampleEntity, String> {

    @Query("SELECT e FROM ExampleEntity e WHERE e.status = :status")
    List<ExampleEntity> findByStatus(@Param("status") ExampleStatusEnum status);
}
```

Rules:
- Always use JPQL, not native SQL, unless absolutely necessary.
- Always use named `@Param` — never positional parameters.

---

## Mapper Pattern

```java
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ExampleMapper {

    @Mapping(target = "createdDate", source = "dateCreated")
    @Mapping(target = "modifiedDate", source = "dateModified")
    ExampleResponseDTO toExampleResponseDTO(ExampleEntity entity);

    // When mapping from a nested entity field:
    // @Mapping(target = "fieldName", source = "relatedEntity.id")

    // Ignore relationship fields the caller sets manually in the service
    @Mapping(target = "relatedEntity", ignore = true)
    ExampleEntity toExampleEntity(ExampleRequestDTO dto);

    // In-place update (for PUT operations)
    @Mapping(target = "relatedEntity", ignore = true)
    void updateEntityFromDto(ExampleRequestDTO dto, @MappingTarget ExampleEntity entity);
}
```

Rules:
- Always use `componentModel = MappingConstants.ComponentModel.SPRING` — never `"default"` or `"spring"` string literals.
- Always use `unmappedTargetPolicy = ReportingPolicy.IGNORE` — audit fields (`id`, `createdBy`, `modifiedBy`, `dateCreated`, `dateModified`, `dbVersion`) are silently skipped, no need to explicitly ignore them.
- Never declare `INSTANCE = Mappers.getMapper(ExampleMapper.class)` — Spring injects the mapper as a bean.
- Always map `dateCreated` → `createdDate` and `dateModified` → `modifiedDate` (Entity → DTO).
- Use individual `@Mapping` annotations — never wrap in `@Mappings({...})`.
- Ignore relationship fields (`@ManyToOne`, `@OneToMany`) when mapping DTO → Entity; set them manually in the service after saving.
- Use `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE` on the mapper when the update method must skip null inbound fields, or on a `@BeanMapping` for a single update method; use `SET_TO_NULL` on a `@BeanMapping` when the update must be a full replace.
- Always add a `void updateEntityFromDto(RequestDTO dto, @MappingTarget Entity entity)` method for PUT operations — avoids creating a new entity and losing unset fields.
- Method names must be descriptive and include the entity/DTO type: `toExampleResponseDTO`, `toExampleEntity`, `updateEntityFromDto`.

---

## Error Handling

Always throw `NextentiException` from the service layer:

```java
throw new NextentiException(
    ApplicationLayer.SERVICE_LAYER,          // DAO_LAYER | SERVICE_LAYER | BUSINESS_LAYER
    ErrorCodeMapping.DAO_NOT_FOUND,          // see complete list below
    "message.key.from.messages.properties", // must exist in messages.properties
    "optionalArg1"                           // optional substitution args ({0}, {1} in message)
);
```

**`ApplicationLayer` values (exact enum names):**
- `DAO_LAYER` — thrown from repository-adjacent logic
- `SERVICE_LAYER` — thrown from service methods (most common)
- `BUSINESS_LAYER` — thrown from business rule validation

**Complete `ErrorCodeMapping` values:**

| Constant | HTTP | Use for |
|----------|------|---------|
| `DAO_NOT_FOUND` | 404 | Record not found by ID |
| `DAO_RECORD_NOT_FOUND` | 400 | Query returned no results |
| `DAO_RECORD_EXISTS` | 400 | Duplicate / unique constraint violation |
| `DAO_SAVE_FAILED` | 500 | Persistence failure on create |
| `DAO_UPDATE_FAILED` | 500 | Persistence failure on update |
| `DAO_DELETE_FAILED` | 500 | Persistence failure on delete |
| `DAO_GET_LIST_FAILED` | 500 | Failure fetching a list |
| `DAO_FIND_FAILED` | 500 | Failure on a find query |
| `DAO_CONFLICT` | 409 | Data-level conflict |
| `SERVICE_CONFLICT` | 409 | Business-level conflict |
| `SERVICE_UNSUPPORTED_MEDIA_TYPE` | 415 | Request `Content-Type` not supported |
| `SERVICE_UNAUTHORIZED_ACTION` | 401 | Auth/permission failure |
| `SERVICE_FORBIDDEN_ACTION` | 403 | Forbidden operation |
| `SERVICE_INVALID_INPUT` | 400 | Invalid input detected in service |
| `VALIDATION_FAILED` | 400 | Business rule validation failure |
| `INVALID_STATE` | 400 | Entity in wrong state for the operation |
| `HEADERS_NOT_CORRECT` | 400 | Required headers missing or wrong |
| `UNEXPECTED_ERROR` | 500 | Catch-all for unexpected failures |

New message keys must be added to `src/main/resources/messages.properties`.  
Never throw raw `RuntimeException` or `Exception` — always use `NextentiException`.

---

## Security Rules

- All new endpoints are protected by default (Spring Security configuration covers this).
- To make an endpoint public, add the path to `SecurityConfiguration` in the `.requestMatchers()` chain.
- Currently public: `/auth/**`, `/social/**`, `/otp/**`, `/aadhaar/**`, `/customers/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/jobs/details`, `/jobs/suggested`, `/ntconfig/country-codes`, `/actuator/**`
- Always use `@PreAuthorize("hasRole('ADMIN')")` on every endpoint method for role-based access control.
- Never log passwords, tokens, or PII fields.
- All password hashing must use `BCryptPasswordEncoder` — never plain text or MD5/SHA1.

---

## Validation Rules

Use Bean Validation annotations on DTO fields:
```java
@NotBlank(message = "{field.required}")
@Email(message = "{email.invalid}")
@Size(min = 8, max = 50, message = "{password.size}")
@Pattern(regexp = "^[0-9]+$", message = "{phone.invalid}")
@Min(value = 1, message = "{field.min}")
private String field;
```

- Apply `@Valid` on every `@RequestBody` parameter in controllers.
- Apply `@Valid` on nested collections/objects inside a request DTO so element-level constraints are enforced.
- Prefer DTO-level Bean Validation over hand-written service guards for simple presence/range checks; keep the service for cross-field and jobType-driven rules only.
- Custom validators go in `com.nextenti.services.common.validation`.
- Validation message keys must exist in `messages.properties`.

---

## Enums

All enums live in `com.nextenti.services.common.enums` or its subpackages. Always check for an existing enum before creating a new one.

**Enum Structure — always use this pattern**

Every enum must implement `EnumValueHandler` and follow this exact structure:

```java
/**
 * Represents the lifecycle status of a candidate profile.
 */
public enum CandidateStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    CandidateStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() { return value; }

    @JsonCreator
    public static CandidateStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(CandidateStatusEnum.class, value);
    }
}
```

**Rules:**
- Always `implements EnumValueHandler` — never a plain enum.
- Always define a `private final String value` constructor field with a lowercase snake_case string.
- Always annotate `getValue()` with both `@Override` and `@JsonValue`.
- Always add a `@JsonCreator public static XxxEnum fromValue(String value)` that delegates to `EnumValueHandler.fromValue(XxxEnum.class, value)` — never inline the lookup logic.
- The `EnumValueHandler.fromValue()` method performs case-insensitive matching against both enum values and names, and throws `IllegalArgumentException` for null/blank input.

Always persist enums as strings: `@Enumerated(EnumType.STRING)`.

**Enum values are always stored UPPERCASE in the database.** `@Enumerated(EnumType.STRING)` gives this for free (it stores `name()`). When a column is mapped as `String` instead of the enum type, persist `enumConstant.name()` — never `getValue()`, whose lowercase display form is for JSON only.

---

## Lombok

| Annotation | Where |
|---|---|
| `@Getter @Setter @EqualsAndHashCode(callSuper = true) @SuperBuilder @NoArgsConstructor @AllArgsConstructor` | Entities extending SrBaseEntity |
| `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` | Simple DTOs without builder |
| `@Data @Builder` | Read-only response DTOs assembled entirely in the service |
| Do NOT use `@RequiredArgsConstructor` on controllers or services — write explicit constructors |

---

## Code Comments

**MANDATORY — no exceptions.** Every Java file written or modified must have Javadoc on **every class** and **every method**. A class or method without Javadoc is incomplete.

### Controller — class + method Javadoc
```java
/**
 * REST controller for managing candidate profiles.
 * Handles HTTP requests and delegates all business logic to {@link CandidateService}.
 */
@RestController
@RequestMapping("/candidates")
@SecurityRequirement(name = "bearerAuth")
public class CandidateController {

    /**
     * Retrieves a candidate profile by its unique identifier.
     *
     * @param id      the UUID of the candidate
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link CandidateResponseDTO}
     * @throws NextentiException if the candidate is not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> getCandidate(@PathVariable UUID id,
                                               @RequestHeader HttpHeaders headers) throws NextentiException { ... }
}
```

### Service — class + method Javadoc
```java
/**
 * Service layer for candidate profile business logic.
 * Coordinates between {@link CandidateRepository} and {@link CandidateMapper}.
 */
@Service
public class CandidateService {

    /**
     * Fetches a candidate entity by ID and converts it to a response DTO.
     *
     * @param id the UUID of the candidate
     * @return the {@link CandidateResponseDTO} for the found candidate
     * @throws NextentiException if no candidate exists with the given ID
     */
    public CandidateResponseDTO getCandidate(UUID id) throws NextentiException { ... }
}
```

### Repository — class + method Javadoc
```java
/**
 * Spring Data JPA repository for {@link CandidateEntity}.
 * Contains only database query definitions — no business logic.
 */
public interface CandidateRepository extends JpaRepository<CandidateEntity, UUID> {

    /**
     * Finds all candidates with the given status.
     *
     * @param status the candidate status to filter by
     * @return list of matching {@link CandidateEntity} records
     */
    @Query("SELECT c FROM CandidateEntity c WHERE c.status = :status")
    List<CandidateEntity> findByStatus(@Param("status") CandidateStatusEnum status);
}
```

### Mapper — class + method Javadoc
```java
/**
 * MapStruct mapper for converting between {@link CandidateEntity} and candidate DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CandidateMapper {

    /**
     * Converts a {@link CandidateEntity} to a {@link CandidateResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    @Mapping(target = "createdDate", source = "dateCreated")
    @Mapping(target = "modifiedDate", source = "dateModified")
    CandidateResponseDTO toCandidateResponseDTO(CandidateEntity entity);
}
```

### Entity — class Javadoc only (no field Javadoc)

Entity fields must **not** have Javadoc comments. The table name and column mapping are self-documenting through JPA annotations.

```java
/**
 * JPA entity representing a candidate profile stored in the sr_candidates table.
 */
@Entity
@Table(name = "sr_candidates")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEntity extends SrBaseEntity {

    @Column(name = "full_name")
    private String fullName;
}
```

### DTO — class Javadoc
```java
/**
 * Response DTO for candidate profile data returned to API consumers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponseDTO extends BaseDTO { ... }
```

### Enum — class Javadoc only (no constant Javadoc)

Enum **constants** must **not** have Javadoc — the constant name and its wire value are self-documenting. Only the enum **class** carries a class-level Javadoc.

```java
/**
 * Represents the lifecycle status of a candidate profile.
 */
public enum CandidateStatusEnum {

    ACTIVE,

    INACTIVE
}
```

### Test class + method Javadoc
```java
/**
 * Unit tests for {@link CandidateController} covering all REST endpoints.
 */
@WebMvcTestBase(controllers = CandidateController.class)
@Import({BaseWebMvcTestConfig.class})
class CandidateControllerTest {

    /**
     * Verifies that a valid candidate ID returns HTTP 200 with the correct response body.
     */
    @Test
    @DisplayName("GET /candidates/{id} - Should return 200 OK with data")
    void testGetCandidate_Success() throws Exception { ... }

    /**
     * Verifies that an unknown candidate ID returns HTTP 404 Not Found.
     */
    @Test
    @DisplayName("GET /candidates/{id} - Should return 404 when not found")
    void testGetCandidate_NotFound() throws Exception { ... }
}
```

**Rules:**
- Every class (Controller, Service, Repository, Mapper, Entity, DTO, Enum, Interface, Validator, Util, Test) must have a class-level `/** ... */` Javadoc.
- Every method — public, protected, or private — must have a descriptive `/** ... */` Javadoc.
- Method Javadoc must include `@param` for every parameter, `@return` unless `void`, and `@throws` for every checked exception.
- Enum constants must **not** have field-level `/** ... */` Javadoc.
- Every test class must have a Javadoc naming the class under test.
- Every test method must have a single-sentence `/** ... */` Javadoc describing the scenario verified.
- Do **not** use single-line `//` comments as a substitute for Javadoc on classes or methods.

---

## Code Complexity

**No method may exceed a Cognitive Complexity of 15.** SonarLint/SonarQube flags anything above that as `Refactor this function to reduce its Cognitive Complexity from N to the 15 allowed`.

What actually drives the number down:

- **Extract nested closures/inner functions into their own top-level methods.**
- **Split the orchestration from the work.** The public method should read as a list of calls — load, collect, write, log.
- **Replace a run of parallel `if`s with a data table plus one loop.**
- **Return early.** A guard clause that returns keeps the rest of the method at nesting level 0 instead of wrapping it in an `else`.

Do not silence the rule with a suppression comment — split the method instead.

---

## Import Rules

**No wildcard imports anywhere in the codebase.** Always import each class individually.

```java
// WRONG
import jakarta.persistence.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

// CORRECT
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
```

This applies to **all** packages: `jakarta.*`, `java.*`, `lombok.*`, `org.springframework.*`, and all project packages (`com.nextenti.*`).

---

## Build Rules (from build.gradle)

### Adding dependencies
- Runtime dependency: `implementation 'group:artifact:version'` 
- Compile-time annotation processor: `annotationProcessor 'group:artifact:version'` 
- Test-only: `testImplementation 'group:artifact:version'` 
- Never add a dependency that duplicates something already provided by a Spring Boot starter.

### Never pin a version the Spring Boot BOM already manages
Dependencies covered by the `io.spring.dependency-management` BOM are declared **without** a version. An explicit version silently overrides the BOM and holds back CVE fixes on the next Boot upgrade.

To move ahead of the BOM for a security fix, override the BOM property instead of pinning the coordinate — declared at the top of `build.gradle` with the CVE/advisory as the comment.

### Annotation processor order — critical
The build uses `lombok-mapstruct-binding:0.2.0` to guarantee Lombok runs before MapStruct during compilation. If you add a new annotation processor, add it **after** the existing MapStruct and Lombok entries to preserve this order.

### Deprecation warnings are enforced
`-Xlint:deprecation` is active on every build. Never use deprecated APIs — the build will warn and reviewers will flag it.

### Always use `jakarta.*` — never `javax.*` 
This is Spring Boot 3.x / Jakarta EE 10. All imports must use the `jakarta` namespace:
- `jakarta.validation.constraints.*` — NOT `javax.validation.constraints.*` 
- `jakarta.persistence.*` — NOT `javax.persistence.*` 
- `jakarta.servlet.*` — NOT `javax.servlet.*` 
- `jakarta.mail.*` — NOT `javax.mail.*` 

### No wildcard imports — always use individual imports
Wildcard imports (`import foo.bar.*`) are **not permitted** anywhere in the codebase. Always import each class individually.

---

## Date handling
`SrBaseEntity` uses `java.time.OffsetDateTime`. Use `java.time.OffsetDateTime` for consistency across entities and DTOs. IST↔UTC conversions and date/time-string parsing are centralized in `DateTimeUtil` (`common.util`) — reuse it rather than re-implementing formatters.

### Batch fetch size
`application.properties` sets `spring.jpa.properties.hibernate.default_batch_fetch_size=100` to batch lazy-association loads and avoid N+1 queries.

### Schema is validated, not generated
`application.properties` sets `spring.jpa.hibernate.ddl-auto=validate` — Hibernate never creates or alters tables. Any new column or table must exist in the database (via SQL migration) before the application will start.

### Test database
Tests run against **H2 in-memory database**, not PostgreSQL. Do not write native PostgreSQL SQL in `@Query` annotations — it will fail in tests. Use JPQL only.

---

## Custom Slash Commands Available

| Command | Usage example | Purpose |
|---|---|---|
| `/new-branch` | `/new-branch feature rishikesh CR2524 AddPayment` | Create a branch from latest main |
| `/commit` | `/commit` or `/commit added payment service` | Stage changes and commit with a generated message |
| `/new-feature` | `/new-feature Payment` | Scaffold all layers for a new domain |
| `/add-endpoint` | `/add-endpoint POST /jobs/apply Apply for a job` | Add one endpoint to an existing feature |
| `/add-entity` | `/add-entity Review userprofile Fields: rating (int)` | Create entity + repository + SQL table |
| `/write-tests` | `/write-tests CandidateController` | Write full test coverage for a class |

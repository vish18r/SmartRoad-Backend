# Javadoc Conventions — Nextenti JavaApp

> **MANDATORY — No exceptions.**  
> Every Java file written or modified must have Javadoc on **every class** and **every method** — Controllers, Services, Repositories, Mappers, Entities, DTOs, Enums, Interfaces, Validators, Utilities, and Test classes/methods. If you write a class or method without Javadoc, it is considered incomplete.

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

Entity fields must **not** have Javadoc comments. The table name and column mapping are self-documenting through JPA annotations. Adding Javadoc to entity files creates noise and maintenance overhead with no benefit.

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

This exception applies **only** to entity classes. All other class types (Controllers, Services, Repositories, Mappers, DTOs, Enums, Interfaces, Validators, Utils, Tests) still require full Javadoc.

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
- Every class (Controller, Service, Repository, Mapper, Entity, DTO, Enum, Interface, Validator, Util, Test) must have a class-level `/** ... */` Javadoc that contains **only** the `@author` and `@version` tags — no descriptive prose. Example:
  ```java
  /**
   * @author 
   * @version 1.0
   */
  ```
- Every method — public, protected, or private — must have a descriptive `/** ... */` Javadoc.
- Method Javadoc must include `@param` for every parameter, `@return` unless `void`, and `@throws` for every checked exception.
- Enum constants must **not** have field-level `/** ... */` Javadoc. Like entity fields, the constant name and its wire value are self-documenting; only the enum **class** needs a class-level Javadoc.
- Every test class must have a Javadoc naming the class under test.
- Every test method must have a single-sentence `/** ... */` Javadoc describing the scenario verified.
- Do **not** use single-line `//` comments as a substitute for Javadoc on classes or methods.

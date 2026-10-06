# Controller Audit & Fixes Required

## Current Issues Found

### AuthController.java
- ❌ No class javadoc
- ❌ No logger
- ❌ No @LogRequestTime annotations
- ❌ No logger.info calls in methods
- ❌ No @RequestHeader HttpHeaders headers
- ❌ Wildcard imports (auth/*)
- ❌ Returns NextentiApiResponse instead of ResponseEntity
- ❌ Missing @SecurityRequirement(name = "bearerAuth")
- ❌ No proper exception handling pattern

## Pattern to Follow (From Reference Controllers)

```java
/**
 * REST controller for [domain] management.
 * Handles HTTP requests and delegates all business logic to services.
 *
 * @author [author]
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/[path]")
@SecurityRequirement(name = "bearerAuth")
public class [Name]Controller {

    private static final Logger logger = LoggerFactory.getLogger([Name]Controller.class);

    private final [Service]Service service;

    public [Name]Controller([Service]Service service) {
        this.service = service;
    }

    /**
     * [Method description]
     *
     * @param id the UUID of the resource
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the response DTO
     * @throws NextentiException if resource not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @LogRequestTime
    public ResponseEntity<Object> getById(@PathVariable UUID id,
                                         @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside getById method--");

        [ResponseDTO] response = service.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
```

## Controllers to Fix

1. ✅ AuthenticationController.java (already follows pattern - reference)
2. ✅ UserController.java (already follows pattern - reference)
3. ✅ SocialSignInController.java (already follows pattern - reference)
4. ❌ AuthController.java - NEEDS REWRITE
5. ❌ OAuth2Controller.java - NEEDS CHECK
6. ❌ BoqController.java - NEEDS CHECK
7. ❌ ClientController.java - NEEDS CHECK
8. ❌ HealthController.java - NEEDS CHECK
9. ❌ OrganizationController.java - NEEDS CHECK
10. ❌ ProjectController.java - NEEDS CHECK
11. ❌ RoadController.java - NEEDS CHECK

## Action Items

### Phase 1: Audit & Plan
- [x] Identify all controllers
- [x] Compare against pattern
- [ ] Document all issues
- [ ] Create fix templates

### Phase 2: Fix Controllers
- [ ] AuthController.java
- [ ] OAuth2Controller.java
- [ ] BoqController.java
- [ ] ClientController.java
- [ ] HealthController.java
- [ ] OrganizationController.java
- [ ] ProjectController.java
- [ ] RoadController.java

### Phase 3: Implement Missing APIs
- [ ] Implement all 40+ API modules as per requirements

### Phase 4: Testing & Build
- [ ] Unit tests
- [ ] Integration tests
- [ ] Build verification
- [ ] Runtime verification

## Standards to Apply

✅ Javadoc on every class and method
✅ Logger with LoggerFactory
✅ @LogRequestTime on every endpoint
✅ logger.info("--Inside methodName method--") first line
✅ @SecurityRequirement(name = "bearerAuth") on class
✅ @PreAuthorize("hasRole('ADMIN')") on every method
✅ @RequestHeader HttpHeaders headers on every method
✅ No wildcard imports
✅ Explicit constructor injection
✅ ResponseEntity<Object> return type
✅ @Valid on request body
✅ NextentiException for errors
✅ Proper HTTP status codes
✅ UUID for IDs
✅ DTOs for requests/responses


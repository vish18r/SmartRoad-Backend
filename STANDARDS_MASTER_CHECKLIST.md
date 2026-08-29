# 🎯 NEXTENTI JAVA STANDARDS - MASTER CHECKLIST

## **Complete Reference for All Code**

---

## **1. ENTITIES** ✅ (In Progress)

### **Naming**
- [ ] All entity classes end with `Entity`
- [ ] Examples: `UserEntity`, `ProjectEntity`, `BoqEntity`

### **Structure**
- [ ] Extends `SrBaseEntity` (NOT `NextentiBaseEntity`)
- [ ] `@Entity` and `@Table` annotations
- [ ] `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder`

### **Javadoc**
- [ ] Class: ONLY `@author @version` (NO prose)
- [ ] Fields: NO Javadoc (clean declarations)

### **Column Naming**
- [ ] `@Column(name = "snake_case")`
- [ ] Add `nullable = false` where applicable
- [ ] Table name: `snake_case_plural` (e.g., `organizations`)

### **Enums**
- [ ] Use new enum names: `UserStatusEnum`, `UserTypeEnum`, etc.
- [ ] `@Enumerated(EnumType.STRING)`

### **Imports**
- [ ] NO wildcard imports
- [ ] All imports explicit

---

## **2. ENUMS** ✅ (COMPLETE)

### **Naming**
- [ ] All enums end with `Enum`
- [ ] Examples: `UserStatusEnum`, `PlatformEnum`

### **Structure**
- [ ] Implements `EnumValueHandler`
- [ ] Single `private final String value` field
- [ ] Constructor: `EnumName(String value) { this.value = value; }`

### **Javadoc**
- [ ] Class: ONLY `@author @version` (NO prose)
- [ ] Constants: NO Javadoc

### **Methods**
- [ ] `getValue()`: `@Override @JsonValue`
- [ ] `fromValue()`: `@JsonCreator`

### **String Values**
- [ ] Lowercase: `"active"`, `"direct_signup"`, `"ios"`

### **Database**
- [ ] Stored as UPPERCASE (JPA default)
- [ ] JSON API returns lowercase

**Status**: ✅ 8 enums created and ready

---

## **3. DTOS**

### **Naming**
- [ ] Request: `*RequestDTO`
- [ ] Response: `*ResponseDTO`
- [ ] Examples: `UserRequestDTO`, `ProjectResponseDTO`

### **Javadoc**
- [ ] Class: ONLY `@author @version`
- [ ] Methods: Full javadoc with `@param`, `@return`, `@throws`
- [ ] Fields: NO Javadoc (unless needed for API docs)

### **Annotations**
- [ ] `@JsonInclude(JsonInclude.Include.NON_NULL)`
- [ ] `@Data` or `@Getter @Setter`
- [ ] Use records for simple request DTOs

### **Validation**
- [ ] `@NotBlank`, `@Email`, `@Size`, etc.
- [ ] Messages from `messages.properties`

### **Records (Simple DTOs)**
```java
public record LoginRequestDTO(
    @NotBlank String loginId,
    @NotBlank String password
) {}
```

---

## **4. CONTROLLERS**

### **Naming**
- [ ] Ends with `Controller`
- [ ] Example: `UserController`

### **Javadoc**
- [ ] Class: ONLY `@author @version`
- [ ] Methods: Full javadoc with `@param`, `@return`, `@throws`

### **Annotations**
- [ ] `@RestController`
- [ ] `@RequestMapping("/path")`
- [ ] `@SecurityRequirement(name = "bearerAuth")` (if protected)
- [ ] `@PreAuthorize("hasRole('ADMIN')")` on every method
- [ ] `@LogRequestTime` on every method

### **Method Structure**
```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
@LogRequestTime
public ResponseEntity<Object> getExample(
    @PathVariable UUID id,
    @RequestHeader HttpHeaders headers) throws NextentiException {
    logger.info("--Inside getExample method--");
    // service call
    return ResponseEntity.status(HttpStatus.OK).body(response);
}
```

### **Rules**
- [ ] Zero business logic
- [ ] Constructor injection (NOT @Autowired)
- [ ] Last parameter: `@RequestHeader HttpHeaders headers`
- [ ] First log line: `logger.info("--Inside methodName method--")`
- [ ] GET/PUT/DELETE: `HttpStatus.OK`
- [ ] POST: `HttpStatus.CREATED`

---

## **5. SERVICES**

### **Naming**
- [ ] Ends with `Service`
- [ ] Example: `UserService`

### **Javadoc**
- [ ] Class: ONLY `@author @version`
- [ ] Methods: Full javadoc with `@param`, `@return`, `@throws`

### **Structure**
- [ ] Owns ALL business logic
- [ ] Only service calls repository/mapper
- [ ] Throws `NextentiException` (never raw `RuntimeException`)

### **Annotations**
- [ ] `@Service`
- [ ] `@Transactional` when needed

### **Error Handling**
```java
throw new NextentiException(
    ApplicationLayer.SERVICE_LAYER,
    ErrorCodeMapping.DAO_NOT_FOUND,
    "message.key.from.messages.properties"
);
```

---

## **6. REPOSITORIES**

### **Naming**
- [ ] Ends with `Repository`
- [ ] Example: `UserRepository`

### **Javadoc**
- [ ] Class: ONLY `@author @version`
- [ ] Methods: Full javadoc with `@param`, `@return`, `@throws`

### **Structure**
- [ ] Extends `JpaRepository<EntityType, UUID>`
- [ ] Optionally extends `JpaSpecificationExecutor<EntityType>`

### **Queries**
- [ ] JPQL only (NO native SQL)
- [ ] Named `@Param` (NO positional)
- [ ] Example: `@Query("SELECT u FROM UserEntity u WHERE u.emailId = :emailId")`

### **Rules**
- [ ] NO business logic
- [ ] NO manual mapping
- [ ] NO transaction handling

---

## **7. MAPPERS**

### **Naming**
- [ ] Ends with `Mapper`
- [ ] Example: `UserMapper`

### **Javadoc**
- [ ] Class: ONLY `@author @version`
- [ ] Methods: Full javadoc with `@param`, `@return`, `@throws`

### **Annotations**
```java
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
```

### **Methods**
- [ ] `toEntityFromRequestDTO()`
- [ ] `toResponseDTOFromEntity()`
- [ ] `updateEntityFromDTO()` for PUT operations
- [ ] Map date fields: `dateCreated` → `createdDate`

### **Rules**
- [ ] Spring injection (NO `Mappers.getMapper()`)
- [ ] Ignore base entity fields automatically
- [ ] Map relationships manually in service

---

## **8. IMPORTS**

### **Rule: NO WILDCARD IMPORTS**

❌ WRONG:
```java
import java.util.*;
import org.springframework.web.bind.annotation.*;
```

✅ CORRECT:
```java
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
```

---

## **9. JAVADOC REQUIREMENTS**

### **Every Class Needs:**
```java
/**
 * @author YourName
 * @version 1.0
 */
```

### **Every Method Needs:**
```java
/**
 * Description of what the method does.
 *
 * @param paramName description of param
 * @return description of return value
 * @throws NextentiException when this error occurs
 */
```

### **NO Javadoc On:**
- Entity fields
- Enum constants
- DTO fields (usually)

---

## **10. DATABASE NAMING**

### **Tables**
- [ ] `snake_case_plural`
- [ ] Examples: `organizations`, `user_audit_logs`, `oauth_states`

### **Columns**
- [ ] `snake_case`
- [ ] Examples: `first_name`, `email_id`, `created_by`

### **Enums**
- [ ] Stored as UPPERCASE
- [ ] JSON returns lowercase

---

## **11. EXCEPTIONS**

### **Always Use:**
```java
throw new NextentiException(
    ApplicationLayer.SERVICE_LAYER,  // DAO_LAYER | SERVICE_LAYER | BUSINESS_LAYER
    ErrorCodeMapping.DAO_NOT_FOUND,  // See complete list in CLAUDE.md
    "message.key",                    // Must exist in messages.properties
    "optionalArg1"                    // Optional substitution args
);
```

### **Never Use:**
- ❌ `throw new RuntimeException()`
- ❌ `throw new Exception()`

---

## **12. VALIDATION**

### **DTOs**
```java
public record LoginRequestDTO(
    @NotBlank(message = "{user.loginId.blank}") String loginId,
    @NotBlank(message = "{user.password.blank}") String password
) {}
```

### **Controllers**
- [ ] `@Valid` on all `@RequestBody` parameters

### **Rules**
- [ ] All validation messages must exist in `messages.properties`
- [ ] Use Bean Validation annotations
- [ ] Service validates business rules

---

## **13. LOGGING**

### **Controller First Line**
```java
logger.info("--Inside methodName method--");
```

### **Private Logger**
```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
```

---

## **REFACTORING CHECKLIST**

### **For Existing Code:**
- [ ] Rename all entities to `*Entity`
- [ ] Fix all entity javadoc (ONLY @author @version)
- [ ] Add Lombok annotations to all entities
- [ ] Remove all manual getters/setters from entities
- [ ] Remove all field javadoc from entities
- [ ] Update all DTO javadoc (ONLY @author @version on class)
- [ ] Update all controller javadoc (ONLY @author @version on class)
- [ ] Update all service javadoc (ONLY @author @version on class)
- [ ] Update all mapper javadoc (ONLY @author @version on class)
- [ ] Verify NO wildcard imports anywhere
- [ ] Ensure all exceptions are `NextentiException`
- [ ] Ensure all controllers have `@PreAuthorize`
- [ ] Ensure all methods have logger calls

---

## **RESOURCES PROVIDED**

| Document | Purpose |
|----------|---------|
| `ENUM_STANDARDS.md` | Enum template and rules |
| `ENUM_UPDATE_SUMMARY.md` | Status of 8 enums created |
| `ENUM_CHECKLIST.md` | Verification checklist for enums |
| `ENTITY_REFACTORING_PLAN.md` | What's wrong with entities and how to fix |
| `ENTITY_TEMPLATES.md` | Templates for all 13 entities |
| `ENTITY_UPDATE_SUMMARY.md` | Status and next steps for entities |
| `STANDARDS_MASTER_CHECKLIST.md` | This document |

---

## **STATUS**

### ✅ COMPLETE
- 8 Enums created and formatted
- UserEntity.java created as template
- All documentation provided

### 📋 IN PROGRESS
- Refactoring remaining 12 entities
- Use ENTITY_TEMPLATES.md for each

### 📝 TO DO
- Update all DTOs javadoc
- Update all Controllers javadoc  
- Update all Services javadoc
- Update all Repositories javadoc
- Update all Mappers javadoc
- Verify NO wildcard imports
- Run tests to verify everything works

---

**Use this checklist for every new class you create!** ✅

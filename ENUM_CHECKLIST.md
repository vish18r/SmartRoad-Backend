# Enum Standards Checklist

## **For Every Enum, Verify:**

### **Class Structure**
- [ ] Enum implements `EnumValueHandler`
- [ ] Class-level Javadoc has ONLY `@author @version` (no prose)
- [ ] No imports are wildcards (all explicit)
- [ ] Proper package naming: `com.nextenti.services.common.enums[.subpackage]`

### **Enum Constants**
- [ ] NO Javadoc on any constant (e.g., no `/** Active status */` above `ACTIVE`)
- [ ] Constants use UPPERCASE names: `ACTIVE`, `PENDING`, etc.
- [ ] String values are lowercase: `"active"`, `"pending"`, etc.

### **Fields & Constructor**
- [ ] Single `private final String value` field
- [ ] Constructor: `EnumName(String value) { this.value = value; }`

### **Methods**
- [ ] `getValue()` method has `@Override` and `@JsonValue` annotations
- [ ] `fromValue(String value)` method has `@JsonCreator` annotation
- [ ] Both methods return/delegate to `EnumValueHandler.fromValue()`

### **Imports**
- [ ] Import `EnumValueHandler` from `common.enums`
- [ ] Import `@JsonCreator` and `@JsonValue` from `fasterxml.jackson.annotation`
- [ ] No wildcard imports anywhere

### **Entity Usage**
- [ ] Entity field uses `@Enumerated(EnumType.STRING)`
- [ ] Repository queries use enum values correctly
- [ ] Service methods parse strings with `EnumName.fromValue()`

### **DTO Usage**
- [ ] DTO field is the enum type (not String)
- [ ] Mapper correctly maps entity enum to DTO enum
- [ ] `@JsonInclude` on DTO skips null fields

### **Testing**
- [ ] ✅ Serialize: `userStatus.getValue()` returns `"active"`
- [ ] ✅ Deserialize: `UserStatusEnum.fromValue("active")` returns `ACTIVE`
- [ ] ✅ JSON: `{"status": "active"}` parses correctly
- [ ] ✅ Database: Stores as `'ACTIVE'` (uppercase, JPA default)

---

## **Common Mistakes to Avoid**

❌ **DON'T:**
```java
// Wrong: Javadoc on constant
public enum UserStatusEnum {
    /** User is active */ ACTIVE("active"),  // ❌ NO JAVADOC HERE
    ...
}

// Wrong: Plural/CamelCase strings
ACTIVE("Active"),  // ❌ Should be lowercase
PENDING("PENDING");  // ❌ Should be lowercase

// Wrong: Prose in class javadoc
/**
 * Represents user account status.  // ❌ NO PROSE
 * @author ...
 */
public enum UserStatusEnum { ... }

// Wrong: Wildcard imports
import com.fasterxml.jackson.annotation.*;  // ❌ NO WILDCARDS
import java.util.*;  // ❌ EXPLICIT IMPORTS ONLY
```

✅ **DO:**
```java
/**
 * @author Rishikesh
 * @version 1.0
 */
public enum UserStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(UserStatusEnum.class, value);
    }
}
```

---

## **Quick Reference: All Created Enums**

| Enum | Values | Package |
|------|--------|---------|
| `UserStatusEnum` | ACTIVE, INACTIVE, PENDING, DELETED, BLOCKED | `common.enums.auth` |
| `UserRoleEnum` | ADMIN, USER, ROOT_USER | `common.enums.auth` |
| `UserTypeEnum` | ORGANIZATION, CANDIDATE, NEXTENTI | `common.enums.auth` |
| `UserRegistrationSourceEnum` | DIRECT_SIGNUP, SOCIAL_GOOGLE, SOCIAL_APPLE | `common.enums.auth` |
| `UserAuditLogStatusEnum` | ACTIVE, BLOCKED, DELETED, PENDING, RESTORE, UNBLOCK | `common.enums.auth` |
| `UserCreationMethodEnum` | DIRECT, GOOGLE, APPLE | `common.enums.auth` |
| `PlatformEnum` | IOS, ANDROID, WEB | `common.enums` |
| `OrganizationUserStatusEnum` | ACTIVE, INACTIVE | `common.enums.userprofile` |

---

**Run this checklist after adding each new enum to ensure compliance!** ✅

# Nextenti Enum Standards

## **MANDATORY ENUM STRUCTURE**

Every enum MUST:
1. Implement `EnumValueHandler`
2. Have class-level Javadoc with ONLY `@author @version`
3. Have NO field or constant Javadoc
4. Include `@JsonValue` and `@JsonCreator`
5. Use lowercase string values
6. Store as `EnumType.STRING` in database

---

## **TEMPLATE**

```java
package com.nextenti.services.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author [YourName]
 * @version 1.0
 */
public enum ExampleStatusEnum implements EnumValueHandler {

    ACTIVE("active"),
    INACTIVE("inactive"),
    PENDING("pending");

    private final String value;

    ExampleStatusEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ExampleStatusEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ExampleStatusEnum.class, value);
    }
}
```

---

## **REQUIRED ENUMS FOR YOUR PROJECT**

Based on your code, create these enums in `com.nextenti.services.common.enums`:

| Enum | Location | Values |
|------|----------|--------|
| `UserStatusEnum` | `auth/` | ACTIVE, INACTIVE, PENDING, DELETED, BLOCKED |
| `UserRoleEnum` | `auth/` | ADMIN, USER, ROOT_USER |
| `UserTypeEnum` | `auth/` | ORGANIZATION, CANDIDATE, NEXTENTI |
| `UserRegistrationSourceEnum` | `auth/` | DIRECT_SIGNUP, SOCIAL_GOOGLE, SOCIAL_APPLE |
| `UserAuditLogStatusEnum` | `auth/` | ACTIVE, INACTIVE, BLOCKED, DELETED, PENDING |
| `UserCreationMethodEnum` | `auth/` | DIRECT, GOOGLE, APPLE |
| `CountryCodeEnum` | root | IN, US, UK, ... (with PhoneFormatInfo) |
| `PlatformEnum` | root | IOS, ANDROID, WEB |
| `OrganizationUserStatusEnum` | `userprofile/` | ACTIVE, INACTIVE |

---

## **RULES**

✅ DO:
- Class javadoc: `@author @version` ONLY
- Enum constants: NO javadoc
- Implement `EnumValueHandler`
- Use `@JsonValue` on `getValue()`
- Use `@JsonCreator` on `fromValue()`
- Lowercase string values

❌ DON'T:
- Add field javadoc
- Add constant javadoc
- Use `Enum name()` in persistence (use `getValue()`)
- Create custom lookup logic (use `fromValue()`)


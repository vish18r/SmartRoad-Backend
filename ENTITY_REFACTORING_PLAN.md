# Entity Refactoring Plan

## **CRITICAL VIOLATIONS**

Your entities have **3 major issues**:

### **1. ❌ WRONG CLASS NAMES (No "Entity" suffix)**

| Current | Should Be | Status |
|---------|-----------|--------|
| `User` | `UserEntity` | ❌ WRONG |
| `Boq` | `BoqEntity` | ❌ WRONG |
| `BoqItem` | `BoqItemEntity` | ❌ WRONG |
| `Client` | `ClientEntity` | ❌ WRONG |
| `OAuthState` | `OAuthStateEntity` | ❌ WRONG |
| `Organization` | `OrganizationEntity` | ❌ WRONG |
| `OrganizationMember` | `OrganizationMemberEntity` | ❌ WRONG |
| `Otp` | `OtpEntity` | ❌ WRONG |
| `Project` | `ProjectEntity` | ❌ WRONG |
| `Road` | `RoadEntity` | ❌ WRONG |
| `RoadSection` | `RoadSectionEntity` | ❌ WRONG |
| `Session` | `SessionEntity` | ❌ WRONG |
| `UserAuditLog` | `UserAuditLogEntity` | ❌ WRONG |

### **2. ❌ JAVADOC VIOLATIONS**

Current (WRONG):
```java
/**
 * Entity class representing a user in the system.  // ❌ PROSE NOT ALLOWED
 * 
 * @author
 * @version 1.0
 */
```

Should Be (CORRECT):
```java
/**
 * @author Rishikesh
 * @version 1.0
 */
```

### **3. ❌ MISSING LOMBOK ANNOTATIONS**

Current (WRONG):
```java
public class User extends SrBaseEntity {
    // Manual getters/setters ❌
    // Manual constructor ❌
    @Builder  // ❌ Only @Builder, missing others
    public User(...) { ... }
}
```

Should Be (CORRECT):
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserEntity extends SrBaseEntity {
    // NO manual getters/setters
    // NO manual constructor
}
```

---

## **REFACTORING STEPS**

### **Step 1: Rename All Entity Classes**
All entities MUST end with `Entity`:
- `User.java` → `UserEntity.java`
- `Boq.java` → `BoqEntity.java`
- etc.

### **Step 2: Update Class Names Inside Files**
```java
// OLD:
public class User extends SrBaseEntity { ... }

// NEW:
public class UserEntity extends SrBaseEntity { ... }
```

### **Step 3: Fix Javadoc**
Remove all prose, keep ONLY `@author @version`:
```java
/**
 * @author Rishikesh
 * @version 1.0
 */
public class UserEntity extends SrBaseEntity { ... }
```

### **Step 4: Add Lombok Annotations**
```java
@Entity
@Table(name = "sr_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserEntity extends SrBaseEntity { ... }
```

### **Step 5: Remove Manual Code**
Delete:
- Manual getters/setters
- Manual constructors
- `@Builder` annotation (use `@SuperBuilder` instead)

### **Step 6: Update Imports**
Use explicit imports:
```java
import com.nextenti.services.common.enums.auth.UserStatusEnum;  // ✅ NEW
import com.nextenti.services.common.enums.UserStatus;  // ❌ OLD (delete if exists)
```

---

## **Corrected User.java Template**

```java
package com.nextenti.services.domain.entity;

import com.nextenti.services.common.enums.auth.UserStatusEnum;
import com.nextenti.services.common.enums.auth.UserRoleEnum;
import com.nextenti.services.common.enums.auth.UserTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Rishikesh
 * @version 1.0
 */
@Entity
@Table(name = "sr_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserEntity extends SrBaseEntity {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email_id", unique = true)
    private String emailId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UserStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRoleEnum role;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserTypeEnum userType;

    @Column(name = "email_verified_yn")
    private Boolean emailVerifiedYn;

    @Column(name = "oauth_signin_id")
    private String oauthSigninId;

    @Column(name = "oauth_type")
    private String oauthType;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "end_date")
    private java.util.Date endDate;

    @Column(name = "registration_source")
    private String registrationSource;
}
```

**Key Changes:**
- ✅ Class name: `User` → `UserEntity`
- ✅ Javadoc: ONLY `@author @version`
- ✅ Lombok annotations: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder`
- ✅ No manual getters/setters
- ✅ No manual constructor
- ✅ Updated enum imports to use new enums
- ✅ No field Javadoc (clean declarations)

---

## **File Renaming Checklist**

When you rename files:
1. Rename the `.java` file in IDE (refactor > rename)
2. Update the class name inside
3. Update repository references
4. Update service references
5. Update controller references

All done at once by IDE refactoring! ✅

---

## **SUMMARY**

✅ **All 13 entities need `Entity` suffix**  
✅ **All javadoc needs fixing (remove prose)**  
✅ **All need Lombok annotations**  
✅ **Remove all manual getters/setters**  
✅ **Update all enum imports**  

Ready to apply changes? 🚀

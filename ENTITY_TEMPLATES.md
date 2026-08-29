# Entity Templates - Use These to Refactor All Entities

## **STANDARD ENTITY TEMPLATE**

```java
package com.nextenti.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;

import java.util.UUID;

/**
 * @author [YourName]
 * @version 1.0
 */
@Entity
@Table(name = "table_name_plural")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ExampleEntity extends SrBaseEntity {

    @Column(name = "field_name")
    private String fieldName;
}
```

---

## **ENTITY TEMPLATES BY NAME**

### **1. BoqEntity** (Bill of Quantities)
```java
package com.nextenti.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;

import java.util.UUID;

/**
 * @author [YourName]
 * @version 1.0
 */
@Entity
@Table(name = "boqs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class BoqEntity extends SrBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
}
```

### **2. BoqItemEntity**
```java
@Entity
@Table(name = "boq_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class BoqItemEntity extends SrBaseEntity {

    @Column(name = "boq_id", nullable = false)
    private UUID boqId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private Double rate;

    @Column(nullable = false)
    private Double amount;
}
```

### **3. ClientEntity**
```java
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ClientEntity extends SrBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String address;
}
```

### **4. OAuthStateEntity**
```java
@Entity
@Table(name = "oauth_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OAuthStateEntity extends SrBaseEntity {

    @Column(name = "state", nullable = false, unique = true)
    private String state;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expires_on")
    private OffsetDateTime expiresOn;

    @Column(name = "used_yn")
    private Boolean usedYn;

    @Column(name = "registration_source")
    private String registrationSource;

    @Column(name = "timestamp")
    private Long timestamp;
}
```

### **5. OrganizationEntity**
```java
@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OrganizationEntity extends SrBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website")
    private String website;
}
```

### **6. OrganizationMemberEntity**
```java
@Entity
@Table(name = "organization_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OrganizationMemberEntity extends SrBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "role")
    private String role;
}
```

### **7. OtpEntity**
```java
@Entity
@Table(name = "otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OtpEntity extends SrBaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String code;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "is_verified")
    private Boolean isVerified;
}
```

### **8. ProjectEntity**
```java
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ProjectEntity extends SrBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "budget")
    private Double budget;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private String status;
}
```

### **9. RoadEntity**
```java
@Entity
@Table(name = "roads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class RoadEntity extends SrBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_length")
    private Double totalLength;

    @Column(name = "width")
    private Double width;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private String status;
}
```

### **10. RoadSectionEntity**
```java
@Entity
@Table(name = "road_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class RoadSectionEntity extends SrBaseEntity {

    @Column(name = "road_id", nullable = false)
    private UUID roadId;

    @Column(name = "section_number")
    private Integer sectionNumber;

    @Column(name = "start_km")
    private Double startKm;

    @Column(name = "end_km")
    private Double endKm;

    @Column(name = "length")
    private Double length;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private String status;
}
```

### **11. SessionEntity**
```java
@Entity
@Table(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class SessionEntity extends SrBaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "refresh_token", nullable = false, unique = true)
    private String refreshToken;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "last_activity")
    private OffsetDateTime lastActivity;
}
```

### **12. UserAuditLogEntity**
```java
@Entity
@Table(name = "user_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserAuditLogEntity extends SrBaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "action_done")
    private String actionDone;

    @Enumerated(EnumType.STRING)
    @Column(name = "nt_status")
    private String ntStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "pending_user_yn")
    private Boolean pendingUserYn;
}
```

---

## **STANDARD IMPORTS FOR ALL ENTITIES**

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;  // Only if using enums
import jakarta.persistence.Enumerated;  // Only if using enums
import jakarta.persistence.ForeignKey;  // Only if using @JoinColumn
import jakarta.persistence.JoinColumn;  // Only if using relationships
import jakarta.persistence.ManyToOne;  // Only if using relationships
import jakarta.persistence.OneToMany;  // Only if using relationships
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;
```

---

## **REFACTORING CHECKLIST**

For each entity file:

- [ ] Rename file: `OldName.java` → `OldNameEntity.java`
- [ ] Update class name: `class OldName` → `class OldNameEntity`
- [ ] Fix javadoc: Remove prose, keep ONLY `@author @version`
- [ ] Add annotations: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper = true) @SuperBuilder`
- [ ] Delete manual getters/setters
- [ ] Delete manual constructors
- [ ] Update enum imports to use new enums (with `Enum` suffix)
- [ ] Add `@Column(nullable = false)` where applicable
- [ ] No field Javadoc (clean declarations only)
- [ ] Extends `SrBaseEntity`
- [ ] Table name is `snake_case_plural` (e.g., `organizations`, `oauth_states`)

---

**Use these templates to refactor all 13 entities!** ✅

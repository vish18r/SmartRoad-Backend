package com.smartroad.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.smartroad.services.common.enums.SessionStatus;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a user session stored in the sr_sessions table.
 * Simplified entity without optimistic locking to avoid version conflicts.
 */
@Entity
@Table(name = "sr_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "modified_by")
    private UUID modifiedBy;

    @Column(name = "date_created")
    private java.util.Date dateCreated;

    @Column(name = "date_modified")
    private java.util.Date dateModified;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;
}


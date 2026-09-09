package com.smartroad.services.domain.entity.auth;

import com.smartroad.services.common.enums.SessionStatus;
import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a user session stored in the sr_sessions table.
 */
@Entity
@Table(name = "sr_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SessionEntity extends SmartRoadBaseEntity {

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

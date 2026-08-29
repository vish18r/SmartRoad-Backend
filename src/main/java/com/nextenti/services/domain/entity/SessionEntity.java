package com.nextenti.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SessionEntity extends SmartRoadBaseEntity {

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


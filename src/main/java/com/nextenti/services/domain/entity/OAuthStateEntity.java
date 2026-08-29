package com.nextenti.services.domain.entity;

import com.nextenti.services.common.enums.auth.UserRegistrationSourceEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "oauth_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)

public class OAuthStateEntity extends SmartRoadBaseEntity {

    @Column(name = "state", unique = true, nullable = false)
    private String state;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "expires_on")
    private OffsetDateTime expiresOn;

    @Column(name = "used_yn")
    private Boolean usedYn;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_source")
    private UserRegistrationSourceEnum registrationSource;

    @Column(name = "timestamp")
    private OffsetDateTime timestamp;
}

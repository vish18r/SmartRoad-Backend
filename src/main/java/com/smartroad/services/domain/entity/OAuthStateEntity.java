package com.smartroad.services.domain.entity;

import com.smartroad.services.common.enums.OAuthType;
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
 * JPA entity representing an OAuth state stored in the oauth_states table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_oauth_states")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class OAuthStateEntity extends SmartRoadBaseEntity {

    @Column(name = "state", unique = true, nullable = false)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_type")
    private OAuthType oauthType;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "used")
    private Boolean used;
}

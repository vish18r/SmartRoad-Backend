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
@Table(name = "otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OtpEntity extends SmartRoadBaseEntity {

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


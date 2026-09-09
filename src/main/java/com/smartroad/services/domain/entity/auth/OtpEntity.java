package com.smartroad.services.domain.entity.auth;

import com.smartroad.services.common.enums.OtpFlow;
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
 * JPA entity representing an OTP stored in the otps table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class OtpEntity extends SmartRoadBaseEntity {

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "email_otp")
    private String emailOtp;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "phone_otp")
    private String phoneOtp;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow")
    private OtpFlow flow;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "retry_count")
    private Integer retryCount;
}


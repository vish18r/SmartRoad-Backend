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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * @author Rishikesh
 * @version 1.0
 */
@Entity
@Table(name = "sr_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends SmartRoadBaseEntity {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email_id", nullable = false, unique = true)
    private String emailId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
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
    private Date endDate;

    @Column(name = "registration_source")
    private String registrationSource;
}


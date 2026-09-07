package com.smartroad.services.domain.entity;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;
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

import java.util.UUID;

/**
 * JPA entity representing a business contact stored in the sr_business_contacts table.
 * Stores contact information (founders, additional contacts) associated with a business profile.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_business_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class BusinessContactEntity extends SmartRoadBaseEntity {

    @Column(name = "business_profile_id", nullable = false)
    private UUID businessProfileId;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactRoleEnum contactRole;

    @Column(name = "phone_number_1")
    private String phoneNumber1;

    @Column(name = "phone_number_2")
    private String phoneNumber2;
}

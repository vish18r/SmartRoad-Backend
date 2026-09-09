package com.smartroad.services.domain.entity.business;

import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JPA entity representing a business profile stored in the sr_business_profiles table.
 * Contains core business information such as name, type, and address.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_business_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class BusinessProfileEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "address_street", columnDefinition = "TEXT")
    private String addressStreet;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_pin_code")
    private String addressPinCode;
}

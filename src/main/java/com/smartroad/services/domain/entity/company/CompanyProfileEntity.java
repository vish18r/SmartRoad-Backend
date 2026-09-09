package com.smartroad.services.domain.entity.company;

import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing the company's public brand profile, stored in the
 * sr_company_profiles table. Unlike {@code BusinessProfileEntity} (which is
 * scoped per-organization), this entity is a single, application-wide record
 * describing the company itself for public/unauthenticated display.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_company_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CompanyProfileEntity extends SmartRoadBaseEntity {

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_type")
    private String businessType;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "tagline")
    private String tagline;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
}

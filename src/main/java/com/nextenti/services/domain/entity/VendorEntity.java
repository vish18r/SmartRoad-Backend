package com.nextenti.services.domain.entity;

import com.nextenti.services.common.enums.vendor.VendorTypeEnum;
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
 * JPA entity representing a vendor/supplier stored in the sr_vendors table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class VendorEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "vendor_name", nullable = false)
    private String vendorName;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "gst_number", unique = true)
    private String gstNumber;

    @Column(name = "vendor_type")
    @Enumerated(EnumType.STRING)
    private VendorTypeEnum vendorType;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}

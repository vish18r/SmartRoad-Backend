package com.smartroad.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing an organization stored in the organizations table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class OrganizationEntity extends SmartRoadBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "active")
    private Boolean active;
}


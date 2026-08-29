package com.nextenti.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrganizationEntity extends SmartRoadBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website")
    private String website;
}


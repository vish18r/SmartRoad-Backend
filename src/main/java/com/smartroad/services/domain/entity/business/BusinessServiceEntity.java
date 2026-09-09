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
 * JPA entity representing a business service stored in the sr_business_services table.
 * Stores the services offered by a business profile.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_business_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class BusinessServiceEntity extends SmartRoadBaseEntity {

    @Column(name = "business_profile_id", nullable = false)
    private UUID businessProfileId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;
}

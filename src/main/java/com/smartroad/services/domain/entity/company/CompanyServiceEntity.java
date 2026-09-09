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

import java.util.UUID;

/**
 * JPA entity representing a service offered by the company, stored in the
 * sr_company_services table, associated with a {@link CompanyProfileEntity}.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_company_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CompanyServiceEntity extends SmartRoadBaseEntity {

    @Column(name = "company_profile_id", nullable = false)
    private UUID companyProfileId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}

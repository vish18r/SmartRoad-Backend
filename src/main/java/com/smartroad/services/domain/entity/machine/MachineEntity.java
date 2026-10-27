package com.smartroad.services.domain.entity.machine;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing a machine/plant asset stored in the sr_machines table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_machines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MachineEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MachineStatusEnum status;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_cost")
    private BigDecimal purchaseCost;

    @Column(name = "current_site_id")
    private UUID currentSiteId;

    @Column(name = "current_value")
    private BigDecimal currentValue;

    @Column(name = "working_hours")
    private BigDecimal workingHours;

    @Column(name = "maintenance_status")
    private String maintenanceStatus;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "capacity")
    private String capacity;

    @Column(name = "operator_name")
    private String operatorName;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}

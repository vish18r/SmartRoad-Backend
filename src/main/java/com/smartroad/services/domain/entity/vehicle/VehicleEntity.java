package com.smartroad.services.domain.entity.vehicle;

import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
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

import java.util.UUID;

/**
 * JPA entity representing a vehicle stored in the sr_vehicles table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class VehicleEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "vehicle_type")
    private String vehicleType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleStatusEnum status;

    @Column(name = "current_site_id")
    private UUID currentSiteId;

    @Column(name = "driver_worker_id")
    private UUID driverWorkerId;

    @Column(name = "capacity")
    private String capacity;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}

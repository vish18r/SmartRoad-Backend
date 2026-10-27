package com.smartroad.services.core.dto.vehicle;

import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * Response DTO describing a vehicle returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDTO {

    private UUID id;
    private UUID organizationId;
    private String registrationNumber;
    private String vehicleType;
    private VehicleStatusEnum status;
    private UUID currentSiteId;
    private String currentSiteName;
    private UUID driverWorkerId;
    private String driverName;
    private String capacity;
    private String notes;
    private UUID createdBy;
    private UUID modifiedBy;
    private Date createdDate;
    private Date modifiedDate;
}

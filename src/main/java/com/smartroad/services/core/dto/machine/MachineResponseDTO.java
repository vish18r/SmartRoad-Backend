package com.smartroad.services.core.dto.machine;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO describing a machine returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineResponseDTO {

    private UUID id;
    private UUID organizationId;
    private String name;
    private String type;
    private MachineStatusEnum status;
    private String registrationNumber;
    private LocalDate purchaseDate;
    private BigDecimal purchaseCost;
    private UUID currentSiteId;
    private String currentSiteName;
    private BigDecimal currentValue;
    private BigDecimal workingHours;
    private String maintenanceStatus;
    private String fuelType;
    private String capacity;
    private String operatorName;
    private UUID createdBy;
    private UUID modifiedBy;
    private Date createdDate;
    private Date modifiedDate;
}

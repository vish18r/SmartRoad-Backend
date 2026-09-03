package com.smartroad.services.core.dto.contracts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartroad.services.common.enums.contracts.ContractStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for contract data returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractResponseDTO {

    private UUID id;

    private UUID projectId;

    private String contractNumber;

    private UUID clientId;

    private UUID contractorId;

    private String workOrderNumber;

    private String agreementNumber;

    private BigDecimal contractValue;

    private Date startDate;

    private Date endDate;

    private BigDecimal securityDeposit;

    private BigDecimal retentionPercentage;

    private ContractStatusEnum status;

    private String documentReference;

    private UUID createdBy;

    private UUID modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}

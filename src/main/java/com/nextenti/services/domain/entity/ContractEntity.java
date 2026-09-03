package com.nextenti.services.domain.entity;

import com.nextenti.services.common.enums.contracts.ContractStatusEnum;
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
import java.util.Date;
import java.util.UUID;

/**
 * JPA entity representing a contract stored in the sr_contracts table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ContractEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "contractor_id")
    private UUID contractorId;

    @Column(name = "work_order_number")
    private String workOrderNumber;

    @Column(name = "agreement_number")
    private String agreementNumber;

    @Column(name = "contract_value")
    private BigDecimal contractValue;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "security_deposit")
    private BigDecimal securityDeposit;

    @Column(name = "retention_percentage")
    private BigDecimal retentionPercentage;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractStatusEnum status;

    @Column(name = "document_reference")
    private String documentReference;
}

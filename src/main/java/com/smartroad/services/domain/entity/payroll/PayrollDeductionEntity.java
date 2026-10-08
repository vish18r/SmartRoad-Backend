package com.smartroad.services.domain.entity.payroll;

import com.smartroad.services.common.enums.payroll.DeductionTypeEnum;
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
import java.util.UUID;

/**
 * JPA entity representing one itemized deduction line on a payroll, stored in the
 * sr_payroll_deductions table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_payroll_deductions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PayrollDeductionEntity extends SmartRoadBaseEntity {

    @Column(name = "payroll_id", nullable = false)
    private UUID payrollId;

    @Column(name = "deduction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeductionTypeEnum deductionType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
}

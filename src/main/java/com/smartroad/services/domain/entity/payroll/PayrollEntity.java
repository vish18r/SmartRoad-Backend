package com.smartroad.services.domain.entity.payroll;

import com.smartroad.services.common.enums.payroll.PayrollStatusEnum;
import com.smartroad.services.common.enums.workers.WageTypeEnum;
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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing a worker's calculated payroll for one period, stored in the
 * sr_payrolls table. All monetary and hour fields are computed by
 * {@code PayrollCalculationService} and are never trusted from the client as-is.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_payrolls")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class PayrollEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "payroll_start_date", nullable = false)
    private LocalDate payrollStartDate;

    @Column(name = "payroll_end_date", nullable = false)
    private LocalDate payrollEndDate;

    @Column(name = "wage_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private WageTypeEnum wageType;

    @Column(name = "daily_wage")
    private BigDecimal dailyWage;

    @Column(name = "monthly_wage")
    private BigDecimal monthlyWage;

    @Column(name = "paid_days", nullable = false)
    private BigDecimal paidDays;

    @Column(name = "absent_days", nullable = false)
    private BigDecimal absentDays;

    @Column(name = "total_worked_hours", nullable = false)
    private BigDecimal totalWorkedHours;

    @Column(name = "regular_hours", nullable = false)
    private BigDecimal regularHours;

    @Column(name = "overtime_hours", nullable = false)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_rate", nullable = false)
    private BigDecimal overtimeRate;

    @Column(name = "basic_wage", nullable = false)
    private BigDecimal basicWage;

    @Column(name = "overtime_amount", nullable = false)
    private BigDecimal overtimeAmount;

    @Column(name = "total_allowances", nullable = false)
    private BigDecimal totalAllowances;

    @Column(name = "gross_pay", nullable = false)
    private BigDecimal grossPay;

    @Column(name = "total_deductions", nullable = false)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", nullable = false)
    private BigDecimal netPay;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PayrollStatusEnum status;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
}

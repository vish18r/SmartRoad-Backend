package com.smartroad.services.core.dto.payroll;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartroad.services.common.enums.payroll.PayrollStatusEnum;
import com.smartroad.services.common.enums.workers.WageTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a payroll record, whether persisted or a calculation preview. A preview
 * returned by {@code POST /api/v1/payroll/calculate} carries a null {@code id} and null
 * {@code status}; every other field is always backend-computed.
 *
 * @param id the payroll UUID, null for a calculation preview
 * @param organizationId the organization that owns the worker
 * @param workerId the worker this payroll is for
 * @param employeeId the worker's employee ID, e.g. SR1, snapshotted at calculation time
 * @param workerName the worker's display name
 * @param mobileNumber the worker's mobile number
 * @param siteId the worker's assigned site, or null
 * @param siteName the worker's assigned site name, or null when the site is unknown
 * @param payrollStartDate the first day of the payroll period, inclusive
 * @param payrollEndDate the last day of the payroll period, inclusive
 * @param wageType the worker's wage type at calculation time
 * @param dailyWage the worker's daily wage, present when wageType is DAILY
 * @param monthlyWage the worker's monthly wage, present when wageType is MONTHLY
 * @param attendance the attendance figures the payroll was calculated from
 * @param regularHoursPerDay the regular working hours per day used to split overtime
 * @param overtimeRate the hourly overtime rate applied
 * @param basicWage the basic wage for the period
 * @param overtimeAmount the overtime pay for the period
 * @param allowances the itemized allowance lines
 * @param totalAllowances the sum of all allowance amounts
 * @param grossPay basic wage plus overtime pay plus total allowances
 * @param deductions the itemized deduction lines
 * @param totalDeductions the sum of all deduction amounts
 * @param netPay gross pay minus total deductions
 * @param status the payroll's lifecycle status, null for a calculation preview
 * @param processedAt when the payroll was processed, or null
 * @param approvedAt when the payroll was approved, or null
 * @param paidAt when the payroll was marked paid, or null
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PayrollResponseDTO(
    UUID id,
    UUID organizationId,
    UUID workerId,
    String employeeId,
    String workerName,
    String mobileNumber,
    UUID siteId,
    String siteName,
    LocalDate payrollStartDate,
    LocalDate payrollEndDate,
    WageTypeEnum wageType,
    BigDecimal dailyWage,
    BigDecimal monthlyWage,
    AttendanceBreakdownDTO attendance,
    BigDecimal regularHoursPerDay,
    BigDecimal overtimeRate,
    BigDecimal basicWage,
    BigDecimal overtimeAmount,
    List<PayrollAllowanceDTO> allowances,
    BigDecimal totalAllowances,
    BigDecimal grossPay,
    List<PayrollDeductionDTO> deductions,
    BigDecimal totalDeductions,
    BigDecimal netPay,
    PayrollStatusEnum status,
    OffsetDateTime processedAt,
    OffsetDateTime approvedAt,
    OffsetDateTime paidAt
) {}

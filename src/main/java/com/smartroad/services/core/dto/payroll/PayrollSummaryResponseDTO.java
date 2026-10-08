package com.smartroad.services.core.dto.payroll;

import java.math.BigDecimal;

/**
 * Aggregated payroll figures for the dashboard summary cards, computed over whatever filters
 * the caller applied (period, worker, status, wage type).
 *
 * @param totalWorkers the number of distinct workers with a matching payroll
 * @param totalGrossPayroll the sum of gross pay across matching payrolls
 * @param totalDeductions the sum of total deductions across matching payrolls
 * @param totalNetPay the sum of net pay across matching payrolls
 * @param pendingPayrollCount payrolls not yet paid or cancelled (DRAFT, PROCESSED or APPROVED)
 * @param paidPayrollCount payrolls marked PAID
 */
public record PayrollSummaryResponseDTO(
    long totalWorkers,
    BigDecimal totalGrossPayroll,
    BigDecimal totalDeductions,
    BigDecimal totalNetPay,
    long pendingPayrollCount,
    long paidPayrollCount
) {}

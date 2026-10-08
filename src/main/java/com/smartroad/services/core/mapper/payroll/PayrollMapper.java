package com.smartroad.services.core.mapper.payroll;

import com.smartroad.services.core.dto.payroll.AttendanceBreakdownDTO;
import com.smartroad.services.core.dto.payroll.PayrollAllowanceDTO;
import com.smartroad.services.core.dto.payroll.PayrollComputationDTO;
import com.smartroad.services.core.dto.payroll.PayrollDeductionDTO;
import com.smartroad.services.core.dto.payroll.PayrollResponseDTO;
import com.smartroad.services.domain.entity.payroll.PayrollAllowanceEntity;
import com.smartroad.services.domain.entity.payroll.PayrollDeductionEntity;
import com.smartroad.services.domain.entity.payroll.PayrollEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Assembles {@link PayrollResponseDTO} from a payroll's persisted entity (or a calculation
 * preview) together with the worker and site it belongs to and its allowance and deduction
 * lines. Composed by hand rather than by MapStruct because the response draws on several
 * entities that are never related by a JPA association.
 *
 * @author Vishal
 * @version 1.0
 */
@Component
public class PayrollMapper {

    /**
     * Builds the response DTO for a persisted payroll.
     *
     * @param entity the persisted payroll entity
     * @param worker the worker the payroll belongs to
     * @param siteName the worker's assigned site name, or null when unknown
     * @param allowances the payroll's persisted allowance lines
     * @param deductions the payroll's persisted deduction lines
     * @return the assembled {@link PayrollResponseDTO}
     */
    public PayrollResponseDTO toPayrollResponseDTO(PayrollEntity entity, WorkerEntity worker, String siteName,
                                                   List<PayrollAllowanceEntity> allowances,
                                                   List<PayrollDeductionEntity> deductions) {
        AttendanceBreakdownDTO attendance = new AttendanceBreakdownDTO(
                presentDaysOf(entity),
                entity.getAbsentDays(),
                entity.getPaidDays(),
                entity.getTotalWorkedHours(),
                entity.getRegularHours(),
                entity.getOvertimeHours()
        );

        return new PayrollResponseDTO(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getWorkerId(),
                entity.getEmployeeId(),
                displayName(worker),
                worker == null ? null : worker.getPhoneNumber(),
                worker == null ? null : worker.getAssignedSiteId(),
                siteName,
                entity.getPayrollStartDate(),
                entity.getPayrollEndDate(),
                entity.getWageType(),
                entity.getDailyWage(),
                entity.getMonthlyWage(),
                attendance,
                null,
                entity.getOvertimeRate(),
                entity.getBasicWage(),
                entity.getOvertimeAmount(),
                allowances.stream().map(this::toAllowanceDTO).toList(),
                entity.getTotalAllowances(),
                entity.getGrossPay(),
                deductions.stream().map(this::toDeductionDTO).toList(),
                entity.getTotalDeductions(),
                entity.getNetPay(),
                entity.getStatus(),
                entity.getProcessedAt(),
                entity.getApprovedAt(),
                entity.getPaidAt()
        );
    }

    /**
     * Builds the response DTO for a calculation preview that has not been persisted.
     *
     * @param worker the worker the preview was calculated for
     * @param siteName the worker's assigned site name, or null when unknown
     * @param startDate the first day of the payroll period, inclusive
     * @param endDate the last day of the payroll period, inclusive
     * @param computation the computed figures
     * @param allowances the proposed allowance lines
     * @param deductions the proposed deduction lines
     * @return the assembled preview {@link PayrollResponseDTO}, with a null id and status
     */
    public PayrollResponseDTO toPreviewResponseDTO(WorkerEntity worker, String siteName,
                                                   LocalDate startDate, LocalDate endDate,
                                                   PayrollComputationDTO computation,
                                                   List<PayrollAllowanceDTO> allowances,
                                                   List<PayrollDeductionDTO> deductions) {
        AttendanceBreakdownDTO attendance = new AttendanceBreakdownDTO(
                computation.presentDays(), computation.absentDays(), computation.paidDays(),
                computation.totalWorkedHours(), computation.regularHours(), computation.overtimeHours());

        return new PayrollResponseDTO(
                null,
                worker.getOrganizationId(),
                worker.getId(),
                worker.getEmployeeId(),
                displayName(worker),
                worker.getPhoneNumber(),
                worker.getAssignedSiteId(),
                siteName,
                startDate,
                endDate,
                worker.getWageType(),
                worker.getDailyWage(),
                worker.getMonthlyWage(),
                attendance,
                computation.regularHoursPerDay(),
                computation.overtimeRate(),
                computation.basicWage(),
                computation.overtimeAmount(),
                allowances == null ? List.of() : allowances,
                computation.totalAllowances(),
                computation.grossPay(),
                deductions == null ? List.of() : deductions,
                computation.totalDeductions(),
                computation.netPay(),
                null,
                null,
                null,
                null
        );
    }

    /**
     * Converts a persisted allowance line to its DTO.
     *
     * @param entity the allowance entity
     * @return the mapped {@link PayrollAllowanceDTO}
     */
    public PayrollAllowanceDTO toAllowanceDTO(PayrollAllowanceEntity entity) {
        return new PayrollAllowanceDTO(entity.getId(), entity.getAllowanceType(), entity.getDescription(), entity.getAmount());
    }

    /**
     * Converts a persisted deduction line to its DTO.
     *
     * @param entity the deduction entity
     * @return the mapped {@link PayrollDeductionDTO}
     */
    public PayrollDeductionDTO toDeductionDTO(PayrollDeductionEntity entity) {
        return new PayrollDeductionDTO(entity.getId(), entity.getDeductionType(), entity.getDescription(), entity.getAmount());
    }

    /**
     * A worker's display name.
     *
     * @param worker the worker, or null
     * @return first and last name, or null when the worker is unknown
     */
    private String displayName(WorkerEntity worker) {
        return worker == null ? null
                : (worker.getFirstName() + " " + (worker.getLastName() == null ? "" : worker.getLastName())).trim();
    }

    /**
     * Present days are not stored directly on a persisted payroll; they are exactly the
     * calendar days in the period minus the stored absent days, since every day in the period
     * is counted as either present (at least one closed session) or absent.
     *
     * @param entity the persisted payroll entity
     * @return the number of present days in the payroll period
     */
    private BigDecimal presentDaysOf(PayrollEntity entity) {
        long totalCalendarDays = ChronoUnit.DAYS.between(entity.getPayrollStartDate(), entity.getPayrollEndDate()) + 1;
        return BigDecimal.valueOf(totalCalendarDays).subtract(entity.getAbsentDays());
    }
}

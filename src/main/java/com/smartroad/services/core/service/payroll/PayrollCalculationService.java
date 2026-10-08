package com.smartroad.services.core.service.payroll;

import com.smartroad.services.common.enums.workers.WageTypeEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.payroll.PayrollAllowanceDTO;
import com.smartroad.services.core.dto.payroll.PayrollComputationDTO;
import com.smartroad.services.core.dto.payroll.PayrollDeductionDTO;
import com.smartroad.services.core.dto.tracking.AttendanceDTO;
import com.smartroad.services.core.service.tracking.AttendanceService;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Centralized, backend-authoritative payroll calculation engine. Loads a worker's attendance
 * sessions through {@link AttendanceService} — never re-deriving worked hours itself — and
 * combines them with the worker's wage configuration to produce every figure a payroll needs.
 * The frontend never performs this arithmetic; it only displays what this service returns.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class PayrollCalculationService {

    private static final BigDecimal DEFAULT_REGULAR_HOURS_PER_DAY = BigDecimal.valueOf(8);
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);
    private static final BigDecimal HALF = BigDecimal.valueOf(0.5);
    private static final int MONEY_SCALE = 2;
    private static final int INTERMEDIATE_SCALE = 6;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final AttendanceService attendanceService;

    /**
     * Constructs the service with its required dependency.
     *
     * @param attendanceService the attendance service owning check-in/check-out session rules;
     *                          reused here so payroll never recomputes worked hours on its own
     */
    public PayrollCalculationService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * Calculates every payroll figure for a worker and period. Nothing is persisted here; the
     * caller decides whether the result is a preview or is saved.
     *
     * @param userId the UUID of the requesting user, used to authorize the attendance read
     * @param worker the worker to calculate payroll for; must already be verified active
     * @param startDate the first day of the payroll period, inclusive
     * @param endDate the last day of the payroll period, inclusive
     * @param regularHoursPerDayInput the caller-supplied regular hours per day, or null to default to 8
     * @param overtimeRateInput the caller-supplied overtime rate, or null to use the worker's configured rate
     * @param allowances the allowance lines to include, or null for none
     * @param deductions the deduction lines to include, or null for none
     * @return the computed {@link PayrollComputationDTO}
     * @throws SmartRoadException if the worker's wage configuration cannot support its wage type
     */
    public PayrollComputationDTO calculate(UUID userId, WorkerEntity worker, LocalDate startDate, LocalDate endDate,
                                           BigDecimal regularHoursPerDayInput, BigDecimal overtimeRateInput,
                                           List<PayrollAllowanceDTO> allowances,
                                           List<PayrollDeductionDTO> deductions) throws SmartRoadException {
        requireWageConfig(worker);

        BigDecimal regularHoursPerDay = regularHoursPerDayInput != null ? regularHoursPerDayInput : DEFAULT_REGULAR_HOURS_PER_DAY;
        BigDecimal overtimeRate = overtimeRateInput != null ? overtimeRateInput
                : worker.getOvertimeRate() != null ? worker.getOvertimeRate() : BigDecimal.ZERO;

        List<AttendanceDTO.DailySummary> dailySummaries = attendanceService.summaryForWorker(userId, worker.getId(), startDate, endDate);
        DailyTotals totals = sumDailyTotals(dailySummaries, regularHoursPerDay);

        long totalCalendarDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal absentDays = BigDecimal.valueOf(Math.max(0, totalCalendarDays - totals.presentDays()));

        BigDecimal basicWage = computeBasicWage(worker, totals.paidDays(), totalCalendarDays);
        BigDecimal overtimeAmount = totals.overtimeHours().multiply(overtimeRate);
        BigDecimal totalAllowances = sumAmounts(allowances == null ? null : allowances.stream().map(PayrollAllowanceDTO::amount).toList());
        BigDecimal totalDeductions = sumAmounts(deductions == null ? null : deductions.stream().map(PayrollDeductionDTO::amount).toList());
        BigDecimal grossPay = basicWage.add(overtimeAmount).add(totalAllowances);
        BigDecimal netPay = grossPay.subtract(totalDeductions);

        return new PayrollComputationDTO(
                BigDecimal.valueOf(totals.presentDays()).setScale(MONEY_SCALE, ROUNDING),
                absentDays.setScale(MONEY_SCALE, ROUNDING),
                round(totals.paidDays()),
                round(totals.totalWorkedHours()),
                round(totals.regularHours()),
                round(totals.overtimeHours()),
                round(regularHoursPerDay),
                round(overtimeRate),
                round(basicWage),
                round(overtimeAmount),
                round(totalAllowances),
                round(grossPay),
                round(totalDeductions),
                round(netPay)
        );
    }

    /**
     * Confirms the worker's wage configuration can support its declared wage type.
     *
     * @param worker the worker to check
     * @throws SmartRoadException if the daily or monthly wage required by the worker's wage type is missing
     */
    private void requireWageConfig(WorkerEntity worker) throws SmartRoadException {
        boolean missing = (worker.getWageType() == WageTypeEnum.DAILY && worker.getDailyWage() == null)
                || (worker.getWageType() == WageTypeEnum.MONTHLY && worker.getMonthlyWage() == null);
        if (missing) {
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "payroll.wage.config.missing"
            );
        }
    }

    /**
     * Sums each day's worked hours, split into regular and overtime hours, and derives paid and
     * present days from the worker's attendance sessions. A day whose sessions are all still
     * open (no check-out yet) contributes no worked hours and is not counted as present, since
     * it has no measurable, payable duration yet.
     *
     * @param dailySummaries one entry per date that had at least one attendance session
     * @param regularHoursPerDay the regular working hours per day used to split overtime
     * @return the summed totals across the period
     */
    private DailyTotals sumDailyTotals(List<AttendanceDTO.DailySummary> dailySummaries, BigDecimal regularHoursPerDay) {
        BigDecimal totalWorkedHours = BigDecimal.ZERO;
        BigDecimal regularHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;
        BigDecimal paidDays = BigDecimal.ZERO;
        int presentDays = 0;

        for (AttendanceDTO.DailySummary summary : dailySummaries) {
            BigDecimal dayHours = BigDecimal.valueOf(summary.totalWorkSeconds())
                    .divide(SECONDS_PER_HOUR, INTERMEDIATE_SCALE, ROUNDING);
            if (dayHours.signum() <= 0) {
                continue;
            }
            presentDays++;
            totalWorkedHours = totalWorkedHours.add(dayHours);
            BigDecimal dayRegular = dayHours.min(regularHoursPerDay);
            BigDecimal dayOvertime = dayHours.subtract(dayRegular).max(BigDecimal.ZERO);
            regularHours = regularHours.add(dayRegular);
            overtimeHours = overtimeHours.add(dayOvertime);
            paidDays = paidDays.add(payableFraction(dayHours, regularHoursPerDay));
        }

        return new DailyTotals(presentDays, totalWorkedHours, regularHours, overtimeHours, paidDays);
    }

    /**
     * The fraction of a paid day a day's worked hours earn, rounded to the nearest half day. A
     * day with any closed session always earns at least half a paid day.
     *
     * @param dayHours the hours worked that day
     * @param regularHoursPerDay the regular working hours per day
     * @return the paid-day fraction, in {0.5, 1.0}
     */
    private BigDecimal payableFraction(BigDecimal dayHours, BigDecimal regularHoursPerDay) {
        BigDecimal ratio = dayHours.divide(regularHoursPerDay, INTERMEDIATE_SCALE, ROUNDING).min(BigDecimal.ONE);
        BigDecimal roundedToHalf = ratio.divide(HALF, 0, ROUNDING).multiply(HALF);
        return roundedToHalf.signum() == 0 ? HALF : roundedToHalf;
    }

    /**
     * Computes the basic wage for the period from the worker's wage configuration.
     * A daily-wage worker earns the daily wage for every paid day. A monthly-wage worker earns
     * a per-day share of the monthly wage (monthly wage divided by the calendar days in the
     * period) for every paid day.
     *
     * @param worker the worker whose wage configuration applies
     * @param paidDays the paid days earned in the period
     * @param totalCalendarDays the calendar days in the payroll period
     * @return the basic wage for the period
     */
    private BigDecimal computeBasicWage(WorkerEntity worker, BigDecimal paidDays, long totalCalendarDays) {
        if (worker.getWageType() == WageTypeEnum.MONTHLY) {
            BigDecimal perDayRate = worker.getMonthlyWage().divide(BigDecimal.valueOf(totalCalendarDays), INTERMEDIATE_SCALE, ROUNDING);
            return perDayRate.multiply(paidDays);
        }
        return worker.getDailyWage().multiply(paidDays);
    }

    /**
     * Sums a list of amounts, treating a null or empty list as zero.
     *
     * @param amounts the amounts to sum, possibly null
     * @return the summed amount
     */
    private BigDecimal sumAmounts(List<BigDecimal> amounts) {
        if (amounts == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            sum = sum.add(amount == null ? BigDecimal.ZERO : amount);
        }
        return sum;
    }

    /**
     * Rounds a monetary or hour figure to two decimal places, half-up.
     *
     * @param value the value to round
     * @return the rounded value
     */
    private BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, ROUNDING);
    }

    /**
     * The per-day attendance figures summed across a payroll period.
     *
     * @param presentDays calendar days with at least one closed session
     * @param totalWorkedHours the summed worked hours across all present days
     * @param regularHours the summed regular-hours portion across all present days
     * @param overtimeHours the summed overtime-hours portion across all present days
     * @param paidDays the summed paid-day fractions across all present days
     */
    private record DailyTotals(int presentDays, BigDecimal totalWorkedHours, BigDecimal regularHours,
                               BigDecimal overtimeHours, BigDecimal paidDays) {}
}

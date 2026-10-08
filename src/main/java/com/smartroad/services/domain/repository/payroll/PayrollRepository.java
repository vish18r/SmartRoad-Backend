package com.smartroad.services.domain.repository.payroll;

import com.smartroad.services.common.enums.payroll.PayrollStatusEnum;
import com.smartroad.services.common.enums.workers.WageTypeEnum;
import com.smartroad.services.domain.entity.payroll.PayrollEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PayrollEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface PayrollRepository extends JpaRepository<PayrollEntity, UUID> {

    /**
     * Finds every non-cancelled payroll of a worker whose period overlaps a candidate period,
     * excluding a given payroll (used when updating an existing DRAFT).
     *
     * @param workerId the worker UUID
     * @param startDate the candidate period's start date
     * @param endDate the candidate period's end date
     * @param excludeId a payroll id to leave out of the check, or null to check against all
     * @return the overlapping, non-cancelled payrolls
     */
    @Query("SELECT p FROM PayrollEntity p WHERE p.workerId = :workerId AND p.status <> 'CANCELLED' "
            + "AND p.payrollStartDate <= :endDate AND p.payrollEndDate >= :startDate "
            + "AND (:excludeId IS NULL OR p.id <> :excludeId)")
    List<PayrollEntity> findOverlapping(@Param("workerId") UUID workerId, @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate, @Param("excludeId") UUID excludeId);

    /**
     * Finds a worker's payroll history, most recent period first.
     *
     * @param workerId the worker UUID
     * @return the worker's payrolls, newest period first
     */
    List<PayrollEntity> findByWorkerIdOrderByPayrollStartDateDesc(UUID workerId);

    /**
     * Finds a page of an organization's payrolls matching the optional filters, most recent
     * period first. Any filter left null matches every value for that field.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param employeeId the employee ID filter (case-insensitive, partial), or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @param search a free-text term matched against employee ID, or null
     * @param pageable the page request
     * @return the matching page of payrolls
     */
    @Query("SELECT p FROM PayrollEntity p WHERE p.organizationId = :organizationId "
            + "AND (:workerId IS NULL OR p.workerId = :workerId) "
            + "AND (:employeeId IS NULL OR LOWER(p.employeeId) LIKE LOWER(CONCAT('%', :employeeId, '%'))) "
            + "AND (:status IS NULL OR p.status = :status) "
            + "AND (:wageType IS NULL OR p.wageType = :wageType) "
            + "AND (:startDate IS NULL OR p.payrollEndDate >= :startDate) "
            + "AND (:endDate IS NULL OR p.payrollStartDate <= :endDate) "
            + "AND (:search IS NULL OR LOWER(p.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "ORDER BY p.payrollStartDate DESC")
    Page<PayrollEntity> search(@Param("organizationId") UUID organizationId, @Param("workerId") UUID workerId,
                               @Param("employeeId") String employeeId, @Param("status") PayrollStatusEnum status,
                               @Param("wageType") WageTypeEnum wageType, @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate, @Param("search") String search, Pageable pageable);

    /**
     * Counts the distinct workers with at least one payroll matching the optional filters.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @return the count of distinct matching workers
     */
    @Query("SELECT COUNT(DISTINCT p.workerId) FROM PayrollEntity p WHERE p.organizationId = :organizationId "
            + "AND (:workerId IS NULL OR p.workerId = :workerId) "
            + "AND (:status IS NULL OR p.status = :status) "
            + "AND (:wageType IS NULL OR p.wageType = :wageType) "
            + "AND (:startDate IS NULL OR p.payrollEndDate >= :startDate) "
            + "AND (:endDate IS NULL OR p.payrollStartDate <= :endDate)")
    long countDistinctWorkers(@Param("organizationId") UUID organizationId, @Param("workerId") UUID workerId,
                              @Param("status") PayrollStatusEnum status, @Param("wageType") WageTypeEnum wageType,
                              @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Sums gross pay across the payrolls matching the optional filters.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @return the summed gross pay, or null when no payroll matches
     */
    @Query("SELECT SUM(p.grossPay) FROM PayrollEntity p WHERE p.organizationId = :organizationId "
            + "AND (:workerId IS NULL OR p.workerId = :workerId) "
            + "AND (:status IS NULL OR p.status = :status) "
            + "AND (:wageType IS NULL OR p.wageType = :wageType) "
            + "AND (:startDate IS NULL OR p.payrollEndDate >= :startDate) "
            + "AND (:endDate IS NULL OR p.payrollStartDate <= :endDate)")
    BigDecimal sumGrossPay(@Param("organizationId") UUID organizationId, @Param("workerId") UUID workerId,
                          @Param("status") PayrollStatusEnum status, @Param("wageType") WageTypeEnum wageType,
                          @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Sums total deductions across the payrolls matching the optional filters.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @return the summed total deductions, or null when no payroll matches
     */
    @Query("SELECT SUM(p.totalDeductions) FROM PayrollEntity p WHERE p.organizationId = :organizationId "
            + "AND (:workerId IS NULL OR p.workerId = :workerId) "
            + "AND (:status IS NULL OR p.status = :status) "
            + "AND (:wageType IS NULL OR p.wageType = :wageType) "
            + "AND (:startDate IS NULL OR p.payrollEndDate >= :startDate) "
            + "AND (:endDate IS NULL OR p.payrollStartDate <= :endDate)")
    BigDecimal sumTotalDeductions(@Param("organizationId") UUID organizationId, @Param("workerId") UUID workerId,
                                 @Param("status") PayrollStatusEnum status, @Param("wageType") WageTypeEnum wageType,
                                 @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Sums net pay across the payrolls matching the optional filters.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param status the payroll status filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @return the summed net pay, or null when no payroll matches
     */
    @Query("SELECT SUM(p.netPay) FROM PayrollEntity p WHERE p.organizationId = :organizationId "
            + "AND (:workerId IS NULL OR p.workerId = :workerId) "
            + "AND (:status IS NULL OR p.status = :status) "
            + "AND (:wageType IS NULL OR p.wageType = :wageType) "
            + "AND (:startDate IS NULL OR p.payrollEndDate >= :startDate) "
            + "AND (:endDate IS NULL OR p.payrollStartDate <= :endDate)")
    BigDecimal sumNetPay(@Param("organizationId") UUID organizationId, @Param("workerId") UUID workerId,
                        @Param("status") PayrollStatusEnum status, @Param("wageType") WageTypeEnum wageType,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Counts payrolls matching the optional filters that also hold a given status.
     *
     * @param organizationId the organization UUID
     * @param workerId the worker UUID filter, or null
     * @param wageType the wage type filter, or null
     * @param startDate periods ending on or after this date, or null
     * @param endDate periods starting on or before this date, or null
     * @param status the status to count
     * @return the count of matching payrolls
     */
    @Query("SELECT COUNT(p) FROM PayrollEntity p WHERE p.organizationId = :organizationId "
            + "AND (:workerId IS NULL OR p.workerId = :workerId) "
            + "AND (:wageType IS NULL OR p.wageType = :wageType) "
            + "AND (:startDate IS NULL OR p.payrollEndDate >= :startDate) "
            + "AND (:endDate IS NULL OR p.payrollStartDate <= :endDate) "
            + "AND p.status = :status")
    long countByStatus(@Param("organizationId") UUID organizationId, @Param("workerId") UUID workerId,
                       @Param("wageType") WageTypeEnum wageType, @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate, @Param("status") PayrollStatusEnum status);
}

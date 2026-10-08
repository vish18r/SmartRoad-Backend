package com.smartroad.services.domain.repository.payroll;

import com.smartroad.services.domain.entity.payroll.PayrollDeductionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PayrollDeductionEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface PayrollDeductionRepository extends JpaRepository<PayrollDeductionEntity, UUID> {

    /**
     * Finds all deduction lines for a payroll.
     *
     * @param payrollId the payroll UUID
     * @return list of matching deduction lines
     */
    List<PayrollDeductionEntity> findByPayrollId(UUID payrollId);

    /**
     * Deletes every deduction line belonging to a payroll, so an update can replace them with a
     * fresh set rather than reconciling individual rows.
     *
     * @param payrollId the payroll UUID
     */
    void deleteByPayrollId(UUID payrollId);
}

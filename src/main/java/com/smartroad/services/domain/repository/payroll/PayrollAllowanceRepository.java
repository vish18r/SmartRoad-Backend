package com.smartroad.services.domain.repository.payroll;

import com.smartroad.services.domain.entity.payroll.PayrollAllowanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link PayrollAllowanceEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface PayrollAllowanceRepository extends JpaRepository<PayrollAllowanceEntity, UUID> {

    /**
     * Finds all allowance lines for a payroll.
     *
     * @param payrollId the payroll UUID
     * @return list of matching allowance lines
     */
    List<PayrollAllowanceEntity> findByPayrollId(UUID payrollId);

    /**
     * Deletes every allowance line belonging to a payroll, so an update can replace them with a
     * fresh set rather than reconciling individual rows.
     *
     * @param payrollId the payroll UUID
     */
    void deleteByPayrollId(UUID payrollId);
}

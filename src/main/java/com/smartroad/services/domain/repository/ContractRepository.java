package com.smartroad.services.domain.repository;

import com.smartroad.services.common.enums.contracts.ContractStatusEnum;
import com.smartroad.services.domain.entity.project.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ContractEntity}.
 * Contains only database query definitions — no business logic.
 *
 * @author Vishal
 * @version 1.0
 */
@Repository
public interface ContractRepository extends JpaRepository<ContractEntity, UUID> {

    /**
     * Finds all contracts by project ID.
     *
     * @param projectId the project UUID
     * @return list of contracts for the project
     */
    @Query("SELECT c FROM ContractEntity c WHERE c.projectId = :projectId")
    List<ContractEntity> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Finds a contract by contract number.
     *
     * @param contractNumber the contract number
     * @return the contract if found
     */
    @Query("SELECT c FROM ContractEntity c WHERE c.contractNumber = :contractNumber")
    Optional<ContractEntity> findByContractNumber(@Param("contractNumber") String contractNumber);

    /**
     * Counts contracts belonging to an organization, scoped through their project.
     *
     * @param organizationId the organization UUID
     * @return total count of contracts
     */
    @Query("SELECT COUNT(c) FROM ContractEntity c, ProjectEntity p "
            + "WHERE c.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE")
    long countByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Counts contracts in a given status belonging to an organization.
     *
     * @param organizationId the organization UUID
     * @param status the contract status to filter by
     * @return count of matching contracts
     */
    @Query("SELECT COUNT(c) FROM ContractEntity c, ProjectEntity p "
            + "WHERE c.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE "
            + "AND c.status = :status")
    long countByOrganizationAndStatus(@Param("organizationId") UUID organizationId,
                                      @Param("status") ContractStatusEnum status);

    /**
     * Sums contracted value across all of an organization's contracts.
     *
     * @param organizationId the organization UUID
     * @return summed contract value, or null when no contract carries a value
     */
    @Query("SELECT SUM(c.contractValue) FROM ContractEntity c, ProjectEntity p "
            + "WHERE c.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE")
    BigDecimal sumContractValueByOrganization(@Param("organizationId") UUID organizationId);

    /**
     * Sums contracted value for an organization's contracts in a given status.
     *
     * @param organizationId the organization UUID
     * @param status the contract status to filter by
     * @return summed contract value for the status, or null when none match
     */
    @Query("SELECT SUM(c.contractValue) FROM ContractEntity c, ProjectEntity p "
            + "WHERE c.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE "
            + "AND c.status = :status")
    BigDecimal sumContractValueByOrganizationAndStatus(@Param("organizationId") UUID organizationId,
                                                       @Param("status") ContractStatusEnum status);

    /**
     * Counts an organization's contracts in a given status whose end date falls inside the window.
     *
     * @param organizationId the organization UUID
     * @param status the contract status to filter by
     * @param from the inclusive start of the window
     * @param to the inclusive end of the window
     * @return count of contracts expiring within the window
     */
    @Query("SELECT COUNT(c) FROM ContractEntity c, ProjectEntity p "
            + "WHERE c.projectId = p.id AND p.organizationId = :organizationId AND p.archived = FALSE "
            + "AND c.status = :status AND c.endDate BETWEEN :from AND :to")
    long countExpiringBetween(@Param("organizationId") UUID organizationId,
                              @Param("status") ContractStatusEnum status,
                              @Param("from") Date from,
                              @Param("to") Date to);
}

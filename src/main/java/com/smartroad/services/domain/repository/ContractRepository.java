package com.smartroad.services.domain.repository;

import com.smartroad.services.domain.entity.project.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

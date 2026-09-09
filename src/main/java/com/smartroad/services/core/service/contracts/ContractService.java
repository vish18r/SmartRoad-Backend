package com.smartroad.services.core.service.contracts;

import com.smartroad.services.common.enums.contracts.ContractStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.contracts.ContractRequestDTO;
import com.smartroad.services.core.dto.contracts.ContractResponseDTO;
import com.smartroad.services.domain.entity.project.ContractEntity;
import com.smartroad.services.domain.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for contract business logic.
 * Manages contract creation, retrieval, updating, and deletion operations.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;

    /**
     * Constructs a ContractService with required dependencies.
     *
     * @param contractRepository the contract repository
     */
    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    /**
     * Creates a new contract.
     *
     * @param request the contract request DTO
     * @return the created contract response DTO
     * @throws SmartRoadException if contract number already exists
     */
    public ContractResponseDTO create(ContractRequestDTO request) throws SmartRoadException {
        try {
            if (contractRepository.findByContractNumber(request.contractNumber()).isPresent()) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "contract.number.exists"
                );
            }

            ContractEntity entity = ContractEntity.builder()
                .projectId(request.projectId())
                .contractNumber(request.contractNumber())
                .clientId(request.clientId())
                .contractorId(request.contractorId())
                .workOrderNumber(request.workOrderNumber())
                .agreementNumber(request.agreementNumber())
                .contractValue(request.contractValue())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .securityDeposit(request.securityDeposit())
                .retentionPercentage(request.retentionPercentage())
                .status(ContractStatusEnum.DRAFT)
                .documentReference(request.documentReference())
                .build();

            ContractEntity saved = contractRepository.save(entity);
            return mapToResponseDTO(saved);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "contract.create.failed",
                e
            );
        }
    }

    /**
     * Retrieves a contract by ID.
     *
     * @param id the contract UUID
     * @return the contract response DTO
     * @throws SmartRoadException if contract not found
     */
    public ContractResponseDTO getById(UUID id) throws SmartRoadException {
        return contractRepository.findById(id)
            .map(this::mapToResponseDTO)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "contract.not.found"
            ));
    }

    /**
     * Retrieves all contracts for a project.
     *
     * @param projectId the project UUID
     * @return list of contract response DTOs
     */
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> listByProject(UUID projectId) {
        return contractRepository.findByProjectId(projectId)
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing contract.
     *
     * @param id the contract UUID
     * @param request the contract request DTO
     * @return the updated contract response DTO
     * @throws SmartRoadException if contract not found
     */
    public ContractResponseDTO update(UUID id, ContractRequestDTO request) throws SmartRoadException {
        try {
            ContractEntity entity = contractRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "contract.not.found"
                ));

            entity.setClientId(request.clientId());
            entity.setContractorId(request.contractorId());
            entity.setWorkOrderNumber(request.workOrderNumber());
            entity.setAgreementNumber(request.agreementNumber());
            entity.setContractValue(request.contractValue());
            entity.setStartDate(request.startDate());
            entity.setEndDate(request.endDate());
            entity.setSecurityDeposit(request.securityDeposit());
            entity.setRetentionPercentage(request.retentionPercentage());
            entity.setDocumentReference(request.documentReference());

            ContractEntity updated = contractRepository.save(entity);
            return mapToResponseDTO(updated);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "contract.update.failed",
                e
            );
        }
    }

    /**
     * Deletes a contract by ID.
     *
     * @param id the contract UUID
     * @throws SmartRoadException if contract not found
     */
    public void delete(UUID id) throws SmartRoadException {
        try {
            if (!contractRepository.existsById(id)) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "contract.not.found"
                );
            }
            contractRepository.deleteById(id);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "contract.delete.failed",
                e
            );
        }
    }

    /**
     * Maps a ContractEntity to ContractResponseDTO.
     *
     * @param entity the contract entity
     * @return the response DTO
     */
    private ContractResponseDTO mapToResponseDTO(ContractEntity entity) {
        return ContractResponseDTO.builder()
            .id(entity.getId())
            .projectId(entity.getProjectId())
            .contractNumber(entity.getContractNumber())
            .clientId(entity.getClientId())
            .contractorId(entity.getContractorId())
            .workOrderNumber(entity.getWorkOrderNumber())
            .agreementNumber(entity.getAgreementNumber())
            .contractValue(entity.getContractValue())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .securityDeposit(entity.getSecurityDeposit())
            .retentionPercentage(entity.getRetentionPercentage())
            .status(entity.getStatus())
            .documentReference(entity.getDocumentReference())
            .createdBy(entity.getCreatedBy())
            .modifiedBy(entity.getModifiedBy())
            .createdDate(entity.getDateCreated())
            .modifiedDate(entity.getDateModified())
            .build();
    }
}

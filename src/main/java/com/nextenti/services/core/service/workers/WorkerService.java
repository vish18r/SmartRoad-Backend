package com.nextenti.services.core.service.workers;

import com.nextenti.services.common.enums.workers.WorkerStatusEnum;
import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.workers.WorkerRequestDTO;
import com.nextenti.services.core.dto.workers.WorkerResponseDTO;
import com.nextenti.services.core.mapper.workers.WorkerMapper;
import com.nextenti.services.core.service.organization.OrganizationService;
import com.nextenti.services.domain.entity.WorkerEntity;
import com.nextenti.services.domain.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for worker business logic.
 * Coordinates between {@link WorkerRepository} and {@link WorkerMapper}.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerMapper workerMapper;
    private final OrganizationService organizationService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param workerRepository the worker repository
     * @param workerMapper the worker mapper
     * @param organizationService the organization service
     */
    public WorkerService(WorkerRepository workerRepository, WorkerMapper workerMapper, OrganizationService organizationService) {
        this.workerRepository = workerRepository;
        this.workerMapper = workerMapper;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new worker.
     *
     * @param userId the UUID of the user creating the worker
     * @param request the worker creation request
     * @return the created {@link WorkerResponseDTO}
     * @throws SmartRoadException if validation fails
     */
    @Transactional
    public WorkerResponseDTO create(UUID userId, WorkerRequestDTO request) throws SmartRoadException {
        organizationService.requireMember(userId, request.organizationId());

        if (request.emailId() != null && workerRepository.existsByEmailIdAndOrganizationId(request.emailId(), request.organizationId())) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "worker.email.already.exists"
            );
        }

        WorkerEntity entity = workerMapper.toWorkerEntity(request);
        entity.setId(UUID.randomUUID());
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        entity.setIsDeleted(false);

        workerRepository.save(entity);

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Retrieves all workers in an organization.
     *
     * @param userId the UUID of the user
     * @param organizationId the UUID of the organization
     * @return list of {@link WorkerResponseDTO}
     * @throws SmartRoadException if user is not an organization member
     */
    @Transactional(readOnly = true)
    public List<WorkerResponseDTO> listByOrganization(UUID userId, UUID organizationId) throws SmartRoadException {
        organizationService.requireMember(userId, organizationId);

        return workerRepository.findByOrganizationIdAndNotDeleted(organizationId)
            .stream()
            .map(workerMapper::toWorkerResponseDTO)
            .toList();
    }

    /**
     * Retrieves workers assigned to a site.
     *
     * @param siteId the UUID of the site
     * @return list of {@link WorkerResponseDTO}
     */
    @Transactional(readOnly = true)
    public List<WorkerResponseDTO> listBySite(UUID siteId) {
        return workerRepository.findByAssignedSiteId(siteId)
            .stream()
            .map(workerMapper::toWorkerResponseDTO)
            .toList();
    }

    /**
     * Retrieves a specific worker by ID.
     *
     * @param id the UUID of the worker
     * @return the {@link WorkerResponseDTO}
     * @throws SmartRoadException if worker not found
     */
    @Transactional(readOnly = true)
    public WorkerResponseDTO getById(UUID id) throws SmartRoadException {
        WorkerEntity entity = workerRepository.findById(id)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Updates an existing worker.
     *
     * @param userId the UUID of the user updating the worker
     * @param id the UUID of the worker to update
     * @param request the worker update request
     * @return the updated {@link WorkerResponseDTO}
     * @throws SmartRoadException if worker not found or update fails
     */
    @Transactional
    public WorkerResponseDTO update(UUID userId, UUID id, WorkerRequestDTO request) throws SmartRoadException {
        WorkerEntity entity = workerRepository.findById(id)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));

        organizationService.requireMember(userId, entity.getOrganizationId());

        if (request.emailId() != null && !request.emailId().equals(entity.getEmailId())) {
            if (workerRepository.existsByEmailIdAndOrganizationId(request.emailId(), entity.getOrganizationId())) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                    "worker.email.already.exists"
                );
            }
        }

        workerMapper.updateWorkerEntityFromDto(request, entity);
        entity.setModifiedBy(userId);

        workerRepository.save(entity);

        return workerMapper.toWorkerResponseDTO(entity);
    }

    /**
     * Soft-deletes a worker.
     *
     * @param userId the UUID of the user deleting the worker
     * @param id the UUID of the worker to delete
     * @throws SmartRoadException if worker not found
     */
    @Transactional
    public void delete(UUID userId, UUID id) throws SmartRoadException {
        WorkerEntity entity = workerRepository.findById(id)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "worker.not.found"
            ));

        organizationService.requireMember(userId, entity.getOrganizationId());

        entity.setIsDeleted(true);
        entity.setModifiedBy(userId);

        workerRepository.save(entity);
    }

    /**
     * Gets the count of active workers in an organization.
     *
     * @param organizationId the UUID of the organization
     * @return the count of active workers
     */
    @Transactional(readOnly = true)
    public long getWorkerCountByOrganization(UUID organizationId) {
        return workerRepository.countByOrganizationId(organizationId);
    }
}

package com.smartroad.services.core.service.vehicle;

import com.smartroad.services.common.enums.vehicle.VehicleStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.dto.vehicle.VehiclePageResponseDTO;
import com.smartroad.services.core.dto.vehicle.VehicleRequestDTO;
import com.smartroad.services.core.dto.vehicle.VehicleResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.entity.vehicle.VehicleEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.VehicleRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for vehicle/fleet management.
 * Owns vehicle CRUD and organization-scoped authorization, delegating persistence to {@link VehicleRepository}.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ProjectRepository projectRepository;
    private final WorkerRepository workerRepository;
    private final OrganizationService organizationService;

    /**
     * Constructs a VehicleService with required dependencies.
     *
     * @param vehicleRepository the vehicle repository
     * @param projectRepository the project repository, used to resolve current site names
     * @param workerRepository the worker repository, used to resolve driver names
     * @param organizationService the organization service, used to authorize the caller
     */
    public VehicleService(VehicleRepository vehicleRepository, ProjectRepository projectRepository,
                          WorkerRepository workerRepository, OrganizationService organizationService) {
        this.vehicleRepository = vehicleRepository;
        this.projectRepository = projectRepository;
        this.workerRepository = workerRepository;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new vehicle under an organization.
     *
     * @param userId the UUID of the user creating the vehicle
     * @param request the vehicle creation request
     * @return the created {@link VehicleResponseDTO}
     * @throws SmartRoadException if the caller is not a member of the organization
     */
    @Transactional
    public VehicleResponseDTO create(UUID userId, VehicleRequestDTO request) throws SmartRoadException {
        organizationService.requireMember(userId, request.organizationId());

        VehicleEntity entity = VehicleEntity.builder()
                .organizationId(request.organizationId())
                .registrationNumber(request.registrationNumber())
                .vehicleType(request.vehicleType())
                .status(request.status() == null ? VehicleStatusEnum.ACTIVE : request.status())
                .currentSiteId(request.currentSiteId())
                .driverWorkerId(request.driverWorkerId())
                .capacity(request.capacity())
                .notes(request.notes())
                .isDeleted(false)
                .build();
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);

        return toResponse(vehicleRepository.save(entity));
    }

    /**
     * Retrieves a vehicle by ID.
     *
     * @param id the vehicle UUID
     * @return the {@link VehicleResponseDTO}
     * @throws SmartRoadException if the vehicle is not found
     */
    @Transactional(readOnly = true)
    public VehicleResponseDTO getById(UUID id) throws SmartRoadException {
        return toResponse(requireVehicle(id));
    }

    /**
     * Lists a page of an organization's vehicles, optionally filtered by status and current site.
     *
     * @param organizationId the organization UUID
     * @param status the optional status filter
     * @param currentSiteId the optional current site filter
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link VehiclePageResponseDTO} for the requested page
     */
    @Transactional(readOnly = true)
    public VehiclePageResponseDTO list(UUID organizationId, VehicleStatusEnum status, UUID currentSiteId,
                                       int page, int limit) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<VehicleEntity> results = vehicleRepository.search(organizationId, status, currentSiteId,
                PageRequest.of(pageIndex, pageSize));

        PaginationMetaDTO pagination = new PaginationMetaDTO(
                pageIndex + 1,
                pageSize,
                results.getTotalElements(),
                results.getTotalPages(),
                results.hasNext(),
                results.hasPrevious());

        return new VehiclePageResponseDTO(results.getContent().stream().map(this::toResponse).toList(), pagination);
    }

    /**
     * Updates a vehicle's details.
     *
     * @param userId the UUID of the user performing the update
     * @param id the vehicle UUID
     * @param request the vehicle update request
     * @return the updated {@link VehicleResponseDTO}
     * @throws SmartRoadException if the vehicle is not found
     */
    @Transactional
    public VehicleResponseDTO update(UUID userId, UUID id, VehicleRequestDTO request) throws SmartRoadException {
        VehicleEntity entity = requireVehicle(id);
        entity.setRegistrationNumber(request.registrationNumber());
        entity.setVehicleType(request.vehicleType());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setCurrentSiteId(request.currentSiteId());
        entity.setDriverWorkerId(request.driverWorkerId());
        entity.setCapacity(request.capacity());
        entity.setNotes(request.notes());
        entity.setModifiedBy(userId);

        return toResponse(vehicleRepository.save(entity));
    }

    /**
     * Soft-deletes a vehicle.
     *
     * @param userId the UUID of the user performing the delete
     * @param id the vehicle UUID
     * @throws SmartRoadException if the vehicle is not found
     */
    @Transactional
    public void delete(UUID userId, UUID id) throws SmartRoadException {
        VehicleEntity entity = requireVehicle(id);
        entity.setIsDeleted(true);
        entity.setModifiedBy(userId);
        vehicleRepository.save(entity);
    }

    /**
     * Loads a non-deleted vehicle by ID.
     *
     * @param id the vehicle UUID
     * @return the {@link VehicleEntity}
     * @throws SmartRoadException if the vehicle is not found
     */
    private VehicleEntity requireVehicle(UUID id) throws SmartRoadException {
        return vehicleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "vehicle.not.found"));
    }

    /**
     * Maps a vehicle entity to its response DTO, resolving the current site's name and the
     * driver's name when set.
     *
     * @param entity the vehicle entity
     * @return the vehicle response DTO
     */
    private VehicleResponseDTO toResponse(VehicleEntity entity) {
        String siteName = entity.getCurrentSiteId() == null ? null
                : projectRepository.findById(entity.getCurrentSiteId()).map(ProjectEntity::getName).orElse(null);
        String driverName = entity.getDriverWorkerId() == null ? null
                : workerRepository.findById(entity.getDriverWorkerId())
                        .map(w -> (w.getFirstName() == null ? "" : w.getFirstName())
                                + (w.getLastName() == null ? "" : " " + w.getLastName()))
                        .map(String::trim)
                        .orElse(null);

        return VehicleResponseDTO.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .registrationNumber(entity.getRegistrationNumber())
                .vehicleType(entity.getVehicleType())
                .status(entity.getStatus())
                .currentSiteId(entity.getCurrentSiteId())
                .currentSiteName(siteName)
                .driverWorkerId(entity.getDriverWorkerId())
                .driverName(driverName)
                .capacity(entity.getCapacity())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .createdDate(entity.getDateCreated())
                .modifiedDate(entity.getDateModified())
                .build();
    }
}

package com.smartroad.services.core.service.machine;

import com.smartroad.services.common.enums.machine.MachineStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.machine.MachinePageResponseDTO;
import com.smartroad.services.core.dto.machine.MachineRequestDTO;
import com.smartroad.services.core.dto.machine.MachineResponseDTO;
import com.smartroad.services.core.dto.material.PaginationMetaDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.machine.MachineEntity;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.repository.MachineRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for machine/plant asset management.
 * Owns machine CRUD and organization-scoped authorization, delegating persistence to {@link MachineRepository}.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class MachineService {

    private final MachineRepository machineRepository;
    private final ProjectRepository projectRepository;
    private final OrganizationService organizationService;

    /**
     * Constructs a MachineService with required dependencies.
     *
     * @param machineRepository the machine repository
     * @param projectRepository the project repository, used to resolve current site names
     * @param organizationService the organization service, used to authorize the caller
     */
    public MachineService(MachineRepository machineRepository, ProjectRepository projectRepository,
                          OrganizationService organizationService) {
        this.machineRepository = machineRepository;
        this.projectRepository = projectRepository;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new machine under an organization.
     *
     * @param userId the UUID of the user creating the machine
     * @param request the machine creation request
     * @return the created {@link MachineResponseDTO}
     * @throws SmartRoadException if the caller is not a member of the organization
     */
    @Transactional
    public MachineResponseDTO create(UUID userId, MachineRequestDTO request) throws SmartRoadException {
        organizationService.requireMember(userId, request.organizationId());

        MachineEntity entity = MachineEntity.builder()
                .organizationId(request.organizationId())
                .name(request.name())
                .type(request.type())
                .status(request.status() == null ? MachineStatusEnum.ACTIVE : request.status())
                .registrationNumber(request.registrationNumber())
                .purchaseDate(request.purchaseDate())
                .purchaseCost(request.purchaseCost())
                .currentSiteId(request.currentSiteId())
                .currentValue(request.currentValue())
                .workingHours(request.workingHours())
                .maintenanceStatus(request.maintenanceStatus())
                .fuelType(request.fuelType())
                .capacity(request.capacity())
                .operatorName(request.operatorName())
                .isDeleted(false)
                .build();
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);

        return toResponse(machineRepository.save(entity));
    }

    /**
     * Retrieves a machine by ID.
     *
     * @param id the machine UUID
     * @return the {@link MachineResponseDTO}
     * @throws SmartRoadException if the machine is not found
     */
    @Transactional(readOnly = true)
    public MachineResponseDTO getById(UUID id) throws SmartRoadException {
        return toResponse(requireMachine(id));
    }

    /**
     * Lists a page of an organization's machines, optionally filtered by status and current site.
     *
     * @param organizationId the organization UUID
     * @param status the optional status filter
     * @param currentSiteId the optional current site filter
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link MachinePageResponseDTO} for the requested page
     */
    @Transactional(readOnly = true)
    public MachinePageResponseDTO list(UUID organizationId, MachineStatusEnum status, UUID currentSiteId,
                                       int page, int limit) {
        int pageIndex = Math.max(page, 1) - 1;
        int pageSize = Math.max(limit, 1);
        Page<MachineEntity> results = machineRepository.search(organizationId, status, currentSiteId,
                PageRequest.of(pageIndex, pageSize));

        PaginationMetaDTO pagination = new PaginationMetaDTO(
                pageIndex + 1,
                pageSize,
                results.getTotalElements(),
                results.getTotalPages(),
                results.hasNext(),
                results.hasPrevious());

        return new MachinePageResponseDTO(results.getContent().stream().map(this::toResponse).toList(), pagination);
    }

    /**
     * Updates a machine's details.
     *
     * @param userId the UUID of the user performing the update
     * @param id the machine UUID
     * @param request the machine update request
     * @return the updated {@link MachineResponseDTO}
     * @throws SmartRoadException if the machine is not found
     */
    @Transactional
    public MachineResponseDTO update(UUID userId, UUID id, MachineRequestDTO request) throws SmartRoadException {
        MachineEntity entity = requireMachine(id);
        entity.setName(request.name());
        entity.setType(request.type());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setRegistrationNumber(request.registrationNumber());
        entity.setPurchaseDate(request.purchaseDate());
        entity.setPurchaseCost(request.purchaseCost());
        entity.setCurrentSiteId(request.currentSiteId());
        entity.setCurrentValue(request.currentValue());
        entity.setWorkingHours(request.workingHours());
        entity.setMaintenanceStatus(request.maintenanceStatus());
        entity.setFuelType(request.fuelType());
        entity.setCapacity(request.capacity());
        entity.setOperatorName(request.operatorName());
        entity.setModifiedBy(userId);

        return toResponse(machineRepository.save(entity));
    }

    /**
     * Soft-deletes a machine.
     *
     * @param userId the UUID of the user performing the delete
     * @param id the machine UUID
     * @throws SmartRoadException if the machine is not found
     */
    @Transactional
    public void delete(UUID userId, UUID id) throws SmartRoadException {
        MachineEntity entity = requireMachine(id);
        entity.setIsDeleted(true);
        entity.setModifiedBy(userId);
        machineRepository.save(entity);
    }

    /**
     * Loads a non-deleted machine by ID.
     *
     * @param id the machine UUID
     * @return the {@link MachineEntity}
     * @throws SmartRoadException if the machine is not found
     */
    private MachineEntity requireMachine(UUID id) throws SmartRoadException {
        return machineRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "machine.not.found"));
    }

    /**
     * Maps a machine entity to its response DTO, resolving the current site's name when set.
     *
     * @param entity the machine entity
     * @return the machine response DTO
     */
    private MachineResponseDTO toResponse(MachineEntity entity) {
        String siteName = entity.getCurrentSiteId() == null ? null
                : projectRepository.findById(entity.getCurrentSiteId()).map(ProjectEntity::getName).orElse(null);

        return MachineResponseDTO.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .name(entity.getName())
                .type(entity.getType())
                .status(entity.getStatus())
                .registrationNumber(entity.getRegistrationNumber())
                .purchaseDate(entity.getPurchaseDate())
                .purchaseCost(entity.getPurchaseCost())
                .currentSiteId(entity.getCurrentSiteId())
                .currentSiteName(siteName)
                .currentValue(entity.getCurrentValue())
                .workingHours(entity.getWorkingHours())
                .maintenanceStatus(entity.getMaintenanceStatus())
                .fuelType(entity.getFuelType())
                .capacity(entity.getCapacity())
                .operatorName(entity.getOperatorName())
                .createdBy(entity.getCreatedBy())
                .modifiedBy(entity.getModifiedBy())
                .createdDate(entity.getDateCreated())
                .modifiedDate(entity.getDateModified())
                .build();
    }
}

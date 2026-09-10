package com.smartroad.services.core.service.material;

import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.material.MaterialRequestDTO;
import com.smartroad.services.core.dto.material.MaterialResponseDTO;
import com.smartroad.services.domain.entity.material.MaterialEntity;
import com.smartroad.services.domain.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for material/inventory business logic.
 * Manages material creation, retrieval, updating, and deletion operations.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
@Transactional
public class MaterialService {

    private final MaterialRepository materialRepository;

    /**
     * Constructs a MaterialService with required dependencies.
     *
     * @param materialRepository the material repository
     */
    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    /**
     * Creates a new material.
     *
     * @param organizationId the organization UUID
     * @param request the material request DTO
     * @return the created material response DTO
     * @throws SmartRoadException if material code already exists
     */
    public MaterialResponseDTO create(UUID organizationId, MaterialRequestDTO request) throws SmartRoadException {
        try {
            if (materialRepository.findByMaterialCode(request.materialCode()).isPresent()) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "material.code.exists"
                );
            }

            MaterialEntity entity = MaterialEntity.builder()
                .organizationId(organizationId)
                .materialCode(request.materialCode())
                .materialName(request.materialName())
                .unit(request.unit())
                .category(request.category())
                .description(request.description())
                .minimumStock(request.minimumStock())
                .build();

            MaterialEntity saved = materialRepository.save(entity);
            return mapToResponseDTO(saved);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "material.create.failed",
                e
            );
        }
    }

    /**
     * Retrieves a material by ID.
     *
     * @param id the material UUID
     * @return the material response DTO
     * @throws SmartRoadException if material not found
     */
    public MaterialResponseDTO getById(UUID id) throws SmartRoadException {
        return materialRepository.findById(id)
            .map(this::mapToResponseDTO)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "material.not.found"
            ));
    }

    /**
     * Retrieves all materials for an organization.
     *
     * @param organizationId the organization UUID
     * @return list of material response DTOs
     */
    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> listByOrganization(UUID organizationId) {
        return materialRepository.findByOrganizationId(organizationId)
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Searches an organization's materials by code, name, or category.
     * A blank or missing term returns an empty list rather than the full catalogue.
     *
     * @param organizationId the organization UUID
     * @param query the free-text search term
     * @return list of matching material response DTOs
     */
    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> search(UUID organizationId, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String term = "%" + query.trim().toLowerCase() + "%";
        return materialRepository.search(organizationId, term)
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Reports an organization's materials whose available stock has fallen to or below their
     * configured reorder threshold. Materials with no threshold set are never reported.
     * When a project is supplied, stock is measured on that project alone rather than across all projects.
     *
     * @param organizationId the organization UUID
     * @param projectId the optional project UUID to measure stock against
     * @return list of material response DTOs at or below their reorder threshold
     */
    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> listLowStock(UUID organizationId, UUID projectId) {
        List<MaterialEntity> materials = projectId == null
            ? materialRepository.findLowStock(organizationId)
            : materialRepository.findLowStockByProject(organizationId, projectId);

        return materials.stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing material.
     *
     * @param id the material UUID
     * @param request the material request DTO
     * @return the updated material response DTO
     * @throws SmartRoadException if material not found
     */
    public MaterialResponseDTO update(UUID id, MaterialRequestDTO request) throws SmartRoadException {
        try {
            MaterialEntity entity = materialRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "material.not.found"
                ));

            entity.setMaterialName(request.materialName());
            entity.setUnit(request.unit());
            entity.setCategory(request.category());
            entity.setDescription(request.description());
            entity.setMinimumStock(request.minimumStock());

            MaterialEntity updated = materialRepository.save(entity);
            return mapToResponseDTO(updated);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "material.update.failed",
                e
            );
        }
    }

    /**
     * Deletes a material by ID.
     *
     * @param id the material UUID
     * @throws SmartRoadException if material not found
     */
    public void delete(UUID id) throws SmartRoadException {
        try {
            if (!materialRepository.existsById(id)) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "material.not.found"
                );
            }
            materialRepository.deleteById(id);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "material.delete.failed",
                e
            );
        }
    }

    /**
     * Maps a MaterialEntity to MaterialResponseDTO.
     *
     * @param entity the material entity
     * @return the response DTO
     */
    private MaterialResponseDTO mapToResponseDTO(MaterialEntity entity) {
        return MaterialResponseDTO.builder()
            .id(entity.getId())
            .organizationId(entity.getOrganizationId())
            .materialCode(entity.getMaterialCode())
            .materialName(entity.getMaterialName())
            .unit(entity.getUnit())
            .category(entity.getCategory())
            .description(entity.getDescription())
            .minimumStock(entity.getMinimumStock())
            .createdBy(entity.getCreatedBy())
            .modifiedBy(entity.getModifiedBy())
            .createdDate(entity.getDateCreated())
            .modifiedDate(entity.getDateModified())
            .build();
    }
}

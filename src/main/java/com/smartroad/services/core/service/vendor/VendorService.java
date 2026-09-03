package com.smartroad.services.core.service.vendor;

import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.vendor.VendorRequestDTO;
import com.smartroad.services.core.dto.vendor.VendorResponseDTO;
import com.smartroad.services.domain.entity.VendorEntity;
import com.smartroad.services.domain.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for vendor/supplier business logic.
 * Manages vendor creation, retrieval, updating, and deletion operations.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;

    /**
     * Constructs a VendorService with required dependencies.
     *
     * @param vendorRepository the vendor repository
     */
    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    /**
     * Creates a new vendor.
     *
     * @param organizationId the organization UUID
     * @param request the vendor request DTO
     * @return the created vendor response DTO
     * @throws SmartRoadException if GST number already exists
     */
    public VendorResponseDTO create(UUID organizationId, VendorRequestDTO request) throws SmartRoadException {
        try {
            if (request.gstNumber() != null && vendorRepository.findByGstNumber(request.gstNumber()).isPresent()) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.SERVICE_INVALID_INPUT,
                    "vendor.gst.exists"
                );
            }

            VendorEntity entity = VendorEntity.builder()
                .organizationId(organizationId)
                .vendorName(request.vendorName())
                .contactPerson(request.contactPerson())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .gstNumber(request.gstNumber())
                .vendorType(request.vendorType())
                .paymentTerms(request.paymentTerms())
                .isActive(request.isActive())
                .build();

            VendorEntity saved = vendorRepository.save(entity);
            return mapToResponseDTO(saved);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "vendor.create.failed",
                e
            );
        }
    }

    /**
     * Retrieves a vendor by ID.
     *
     * @param id the vendor UUID
     * @return the vendor response DTO
     * @throws SmartRoadException if vendor not found
     */
    public VendorResponseDTO getById(UUID id) throws SmartRoadException {
        return vendorRepository.findById(id)
            .map(this::mapToResponseDTO)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "vendor.not.found"
            ));
    }

    /**
     * Retrieves all active vendors for an organization.
     *
     * @param organizationId the organization UUID
     * @return list of vendor response DTOs
     */
    @Transactional(readOnly = true)
    public List<VendorResponseDTO> listByOrganization(UUID organizationId) {
        return vendorRepository.findByOrganizationId(organizationId)
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing vendor.
     *
     * @param id the vendor UUID
     * @param request the vendor request DTO
     * @return the updated vendor response DTO
     * @throws SmartRoadException if vendor not found
     */
    public VendorResponseDTO update(UUID id, VendorRequestDTO request) throws SmartRoadException {
        try {
            VendorEntity entity = vendorRepository.findById(id)
                .orElseThrow(() -> new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "vendor.not.found"
                ));

            entity.setVendorName(request.vendorName());
            entity.setContactPerson(request.contactPerson());
            entity.setPhone(request.phone());
            entity.setEmail(request.email());
            entity.setAddress(request.address());
            entity.setCity(request.city());
            entity.setState(request.state());
            entity.setPostalCode(request.postalCode());
            entity.setVendorType(request.vendorType());
            entity.setPaymentTerms(request.paymentTerms());
            entity.setIsActive(request.isActive());

            VendorEntity updated = vendorRepository.save(entity);
            return mapToResponseDTO(updated);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "vendor.update.failed",
                e
            );
        }
    }

    /**
     * Deletes a vendor by ID.
     *
     * @param id the vendor UUID
     * @throws SmartRoadException if vendor not found
     */
    public void delete(UUID id) throws SmartRoadException {
        try {
            if (!vendorRepository.existsById(id)) {
                throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "vendor.not.found"
                );
            }
            vendorRepository.deleteById(id);
        } catch (SmartRoadException e) {
            throw e;
        } catch (Exception e) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INTERNAL_ERROR,
                "vendor.delete.failed",
                e
            );
        }
    }

    /**
     * Maps a VendorEntity to VendorResponseDTO.
     *
     * @param entity the vendor entity
     * @return the response DTO
     */
    private VendorResponseDTO mapToResponseDTO(VendorEntity entity) {
        return VendorResponseDTO.builder()
            .id(entity.getId())
            .organizationId(entity.getOrganizationId())
            .vendorName(entity.getVendorName())
            .contactPerson(entity.getContactPerson())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .address(entity.getAddress())
            .city(entity.getCity())
            .state(entity.getState())
            .postalCode(entity.getPostalCode())
            .gstNumber(entity.getGstNumber())
            .vendorType(entity.getVendorType())
            .paymentTerms(entity.getPaymentTerms())
            .isActive(entity.getIsActive())
            .createdBy(entity.getCreatedBy())
            .modifiedBy(entity.getModifiedBy())
            .createdDate(entity.getDateCreated())
            .modifiedDate(entity.getDateModified())
            .build();
    }
}

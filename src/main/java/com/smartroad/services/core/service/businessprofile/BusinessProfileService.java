package com.smartroad.services.core.service.businessprofile;

import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.businessprofile.BusinessContactRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessContactResponseDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessProfileRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessProfileResponseDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessServiceRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessServiceResponseDTO;
import com.smartroad.services.core.dto.businessprofile.CompleteBusinessProfileResponseDTO;
import com.smartroad.services.core.mapper.businessprofile.BusinessContactMapper;
import com.smartroad.services.core.mapper.businessprofile.BusinessProfileMapper;
import com.smartroad.services.core.mapper.businessprofile.BusinessServiceMapper;
import com.smartroad.services.domain.entity.business.BusinessContactEntity;
import com.smartroad.services.domain.entity.business.BusinessProfileEntity;
import com.smartroad.services.domain.entity.business.BusinessServiceEntity;
import com.smartroad.services.domain.repository.BusinessContactRepository;
import com.smartroad.services.domain.repository.BusinessProfileRepository;
import com.smartroad.services.domain.repository.BusinessServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for business profile business logic.
 * Coordinates between repositories and mappers for business profile operations.
 */
@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessContactRepository businessContactRepository;
    private final BusinessServiceRepository businessServiceRepository;
    private final BusinessProfileMapper businessProfileMapper;
    private final BusinessContactMapper businessContactMapper;
    private final BusinessServiceMapper businessServiceMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param businessProfileRepository repository for business profiles
     * @param businessContactRepository repository for contacts
     * @param businessServiceRepository repository for services
     * @param businessProfileMapper mapper for business profiles
     * @param businessContactMapper mapper for contacts
     * @param businessServiceMapper mapper for services
     */
    public BusinessProfileService(
        BusinessProfileRepository businessProfileRepository,
        BusinessContactRepository businessContactRepository,
        BusinessServiceRepository businessServiceRepository,
        BusinessProfileMapper businessProfileMapper,
        BusinessContactMapper businessContactMapper,
        BusinessServiceMapper businessServiceMapper
    ) {
        this.businessProfileRepository = businessProfileRepository;
        this.businessContactRepository = businessContactRepository;
        this.businessServiceRepository = businessServiceRepository;
        this.businessProfileMapper = businessProfileMapper;
        this.businessContactMapper = businessContactMapper;
        this.businessServiceMapper = businessServiceMapper;
    }

    /**
     * Creates a new business profile.
     *
     * @param userId the ID of the user performing the operation
     * @param request the business profile creation request
     * @return the created business profile response DTO
     * @throws SmartRoadException if creation fails or organization already has a profile
     */
    @Transactional
    public BusinessProfileResponseDTO createBusinessProfile(UUID userId, BusinessProfileRequestDTO request) throws SmartRoadException {
        if (businessProfileRepository.findByOrganizationId(request.organizationId()).isPresent()) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "businessprofile.already.exists"
            );
        }

        BusinessProfileEntity entity = businessProfileMapper.toEntity(request);
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        BusinessProfileEntity savedEntity = businessProfileRepository.save(entity);

        return businessProfileMapper.toResponseDTO(savedEntity);
    }

    /**
     * Retrieves a business profile by ID.
     *
     * @param profileId the ID of the business profile
     * @return the business profile response DTO
     * @throws SmartRoadException if profile not found
     */
    @Transactional(readOnly = true)
    public BusinessProfileResponseDTO getBusinessProfile(UUID profileId) throws SmartRoadException {
        BusinessProfileEntity entity = businessProfileRepository.findById(profileId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            ));
        return businessProfileMapper.toResponseDTO(entity);
    }

    /**
     * Retrieves a business profile by organization ID.
     *
     * @param organizationId the ID of the organization
     * @return the business profile response DTO
     * @throws SmartRoadException if profile not found
     */
    @Transactional(readOnly = true)
    public BusinessProfileResponseDTO getBusinessProfileByOrganization(UUID organizationId) throws SmartRoadException {
        BusinessProfileEntity entity = businessProfileRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            ));
        return businessProfileMapper.toResponseDTO(entity);
    }

    /**
     * Retrieves the complete business profile with all nested data.
     *
     * @param profileId the ID of the business profile
     * @return the complete business profile response DTO with contacts and services
     * @throws SmartRoadException if profile not found
     */
    @Transactional(readOnly = true)
    public CompleteBusinessProfileResponseDTO getCompleteBusinessProfile(UUID profileId) throws SmartRoadException {
        BusinessProfileEntity profile = businessProfileRepository.findById(profileId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            ));

        List<BusinessContactEntity> contacts = businessContactRepository.findByBusinessProfileId(profileId);
        List<BusinessServiceEntity> services = businessServiceRepository.findByBusinessProfileId(profileId);

        List<BusinessContactResponseDTO> contactDTOs = contacts.stream()
            .map(businessContactMapper::toResponseDTO)
            .collect(Collectors.toList());

        List<BusinessServiceResponseDTO> serviceDTOs = services.stream()
            .map(businessServiceMapper::toResponseDTO)
            .collect(Collectors.toList());

        return new CompleteBusinessProfileResponseDTO(
            profile.getId(),
            profile.getOrganizationId(),
            profile.getBusinessName(),
            profile.getBusinessType(),
            profile.getAddressStreet(),
            profile.getAddressCity(),
            profile.getAddressPinCode(),
            contactDTOs,
            serviceDTOs
        );
    }

    /**
     * Updates an existing business profile.
     *
     * @param profileId the ID of the business profile to update
     * @param userId the ID of the user performing the operation
     * @param request the business profile update request
     * @return the updated business profile response DTO
     * @throws SmartRoadException if profile not found
     */
    @Transactional
    public BusinessProfileResponseDTO updateBusinessProfile(UUID profileId, UUID userId, BusinessProfileRequestDTO request) throws SmartRoadException {
        BusinessProfileEntity entity = businessProfileRepository.findById(profileId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            ));

        businessProfileMapper.updateEntityFromDTO(request, entity);
        entity.setModifiedBy(userId);
        BusinessProfileEntity updatedEntity = businessProfileRepository.save(entity);

        return businessProfileMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Deletes a business profile and all associated contacts and services.
     *
     * @param profileId the ID of the business profile to delete
     * @throws SmartRoadException if profile not found
     */
    @Transactional
    public void deleteBusinessProfile(UUID profileId) throws SmartRoadException {
        BusinessProfileEntity entity = businessProfileRepository.findById(profileId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            ));

        businessContactRepository.deleteAll(businessContactRepository.findByBusinessProfileId(profileId));
        businessServiceRepository.deleteAll(businessServiceRepository.findByBusinessProfileId(profileId));
        businessProfileRepository.delete(entity);
    }

    /**
     * Adds a new contact to the business profile.
     *
     * @param userId the ID of the user performing the operation
     * @param request the contact creation request
     * @return the created contact response DTO
     * @throws SmartRoadException if profile not found or contact role already exists
     */
    @Transactional
    public BusinessContactResponseDTO addBusinessContact(UUID userId, BusinessContactRequestDTO request) throws SmartRoadException {
        if (!businessProfileRepository.existsById(request.businessProfileId())) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            );
        }

        if (businessContactRepository.findByBusinessProfileIdAndContactRole(request.businessProfileId(), request.contactRole()).isPresent()) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "businesscontact.role.already.exists"
            );
        }

        BusinessContactEntity entity = businessContactMapper.toEntity(request);
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        BusinessContactEntity savedEntity = businessContactRepository.save(entity);

        return businessContactMapper.toResponseDTO(savedEntity);
    }

    /**
     * Retrieves all contacts for a business profile.
     *
     * @param profileId the ID of the business profile
     * @return list of contact response DTOs
     */
    @Transactional(readOnly = true)
    public List<BusinessContactResponseDTO> getBusinessContacts(UUID profileId) {
        return businessContactRepository.findByBusinessProfileId(profileId).stream()
            .map(businessContactMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing business contact.
     *
     * @param contactId the ID of the contact to update
     * @param userId the ID of the user performing the operation
     * @param request the contact update request
     * @return the updated contact response DTO
     * @throws SmartRoadException if contact not found
     */
    @Transactional
    public BusinessContactResponseDTO updateBusinessContact(UUID contactId, UUID userId, BusinessContactRequestDTO request) throws SmartRoadException {
        BusinessContactEntity entity = businessContactRepository.findById(contactId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businesscontact.not.found"
            ));

        businessContactMapper.updateEntityFromDTO(request, entity);
        entity.setModifiedBy(userId);
        BusinessContactEntity updatedEntity = businessContactRepository.save(entity);

        return businessContactMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Deletes a business contact.
     *
     * @param contactId the ID of the contact to delete
     * @throws SmartRoadException if contact not found
     */
    @Transactional
    public void deleteBusinessContact(UUID contactId) throws SmartRoadException {
        BusinessContactEntity entity = businessContactRepository.findById(contactId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businesscontact.not.found"
            ));

        businessContactRepository.delete(entity);
    }

    /**
     * Adds a new service to the business profile.
     *
     * @param userId the ID of the user performing the operation
     * @param request the service creation request
     * @return the created service response DTO
     * @throws SmartRoadException if profile not found or service already exists
     */
    @Transactional
    public BusinessServiceResponseDTO addBusinessService(UUID userId, BusinessServiceRequestDTO request) throws SmartRoadException {
        if (!businessProfileRepository.existsById(request.businessProfileId())) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessprofile.not.found"
            );
        }

        if (businessServiceRepository.findByBusinessProfileIdAndServiceName(request.businessProfileId(), request.serviceName()).isPresent()) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_VALIDATION_FAILED,
                "businessservice.already.exists"
            );
        }

        BusinessServiceEntity entity = businessServiceMapper.toEntity(request);
        entity.setCreatedBy(userId);
        entity.setModifiedBy(userId);
        BusinessServiceEntity savedEntity = businessServiceRepository.save(entity);

        return businessServiceMapper.toResponseDTO(savedEntity);
    }

    /**
     * Retrieves all services for a business profile.
     *
     * @param profileId the ID of the business profile
     * @return list of service response DTOs
     */
    @Transactional(readOnly = true)
    public List<BusinessServiceResponseDTO> getBusinessServices(UUID profileId) {
        return businessServiceRepository.findByBusinessProfileId(profileId).stream()
            .map(businessServiceMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates an existing business service.
     *
     * @param serviceId the ID of the service to update
     * @param userId the ID of the user performing the operation
     * @param request the service update request
     * @return the updated service response DTO
     * @throws SmartRoadException if service not found
     */
    @Transactional
    public BusinessServiceResponseDTO updateBusinessService(UUID serviceId, UUID userId, BusinessServiceRequestDTO request) throws SmartRoadException {
        BusinessServiceEntity entity = businessServiceRepository.findById(serviceId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessservice.not.found"
            ));

        businessServiceMapper.updateEntityFromDTO(request, entity);
        entity.setModifiedBy(userId);
        BusinessServiceEntity updatedEntity = businessServiceRepository.save(entity);

        return businessServiceMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Deletes a business service.
     *
     * @param serviceId the ID of the service to delete
     * @throws SmartRoadException if service not found
     */
    @Transactional
    public void deleteBusinessService(UUID serviceId) throws SmartRoadException {
        BusinessServiceEntity entity = businessServiceRepository.findById(serviceId)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "businessservice.not.found"
            ));

        businessServiceRepository.delete(entity);
    }
}

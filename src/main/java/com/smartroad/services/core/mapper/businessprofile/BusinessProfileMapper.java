package com.smartroad.services.core.mapper.businessprofile;

import com.smartroad.services.core.dto.businessprofile.BusinessProfileRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessProfileResponseDTO;
import com.smartroad.services.domain.entity.business.BusinessProfileEntity;
import org.springframework.stereotype.Component;

/**
 * Component for converting between {@link BusinessProfileEntity} and business profile DTOs.
 */
@Component
public class BusinessProfileMapper {

    /**
     * Converts a {@link BusinessProfileEntity} to a {@link BusinessProfileResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    public BusinessProfileResponseDTO toResponseDTO(BusinessProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BusinessProfileResponseDTO(
            entity.getId(),
            entity.getOrganizationId(),
            entity.getBusinessName(),
            entity.getBusinessType(),
            entity.getAddressStreet(),
            entity.getAddressCity(),
            entity.getAddressPinCode()
        );
    }

    /**
     * Converts a {@link BusinessProfileRequestDTO} to a {@link BusinessProfileEntity}.
     *
     * @param dto the source request DTO
     * @return the mapped entity
     */
    public BusinessProfileEntity toEntity(BusinessProfileRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BusinessProfileEntity.builder()
            .organizationId(dto.organizationId())
            .businessName(dto.businessName())
            .businessType(dto.businessType())
            .addressStreet(dto.addressStreet())
            .addressCity(dto.addressCity())
            .addressPinCode(dto.addressPinCode())
            .build();
    }

    /**
     * Updates an existing {@link BusinessProfileEntity} from a {@link BusinessProfileRequestDTO}.
     *
     * @param dto the source request DTO
     * @param entity the target entity to update
     */
    public void updateEntityFromDTO(BusinessProfileRequestDTO dto, BusinessProfileEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setBusinessName(dto.businessName());
        entity.setBusinessType(dto.businessType());
        entity.setAddressStreet(dto.addressStreet());
        entity.setAddressCity(dto.addressCity());
        entity.setAddressPinCode(dto.addressPinCode());
    }
}

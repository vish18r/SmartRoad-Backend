package com.smartroad.services.core.mapper.businessprofile;

import com.smartroad.services.core.dto.businessprofile.BusinessContactRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessContactResponseDTO;
import com.smartroad.services.domain.entity.BusinessContactEntity;
import org.springframework.stereotype.Component;

/**
 * Component for converting between {@link BusinessContactEntity} and business contact DTOs.
 */
@Component
public class BusinessContactMapper {

    /**
     * Converts a {@link BusinessContactEntity} to a {@link BusinessContactResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    public BusinessContactResponseDTO toResponseDTO(BusinessContactEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BusinessContactResponseDTO(
            entity.getId(),
            entity.getBusinessProfileId(),
            entity.getContactName(),
            entity.getContactRole(),
            entity.getPhoneNumber1(),
            entity.getPhoneNumber2()
        );
    }

    /**
     * Converts a {@link BusinessContactRequestDTO} to a {@link BusinessContactEntity}.
     *
     * @param dto the source request DTO
     * @return the mapped entity
     */
    public BusinessContactEntity toEntity(BusinessContactRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BusinessContactEntity.builder()
            .businessProfileId(dto.businessProfileId())
            .contactName(dto.contactName())
            .contactRole(dto.contactRole())
            .phoneNumber1(dto.phoneNumber1())
            .phoneNumber2(dto.phoneNumber2())
            .build();
    }

    /**
     * Updates an existing {@link BusinessContactEntity} from a {@link BusinessContactRequestDTO}.
     *
     * @param dto the source request DTO
     * @param entity the target entity to update
     */
    public void updateEntityFromDTO(BusinessContactRequestDTO dto, BusinessContactEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setContactName(dto.contactName());
        entity.setContactRole(dto.contactRole());
        entity.setPhoneNumber1(dto.phoneNumber1());
        entity.setPhoneNumber2(dto.phoneNumber2());
    }
}

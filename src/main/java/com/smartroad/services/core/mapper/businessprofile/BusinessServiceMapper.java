package com.smartroad.services.core.mapper.businessprofile;

import com.smartroad.services.core.dto.businessprofile.BusinessServiceRequestDTO;
import com.smartroad.services.core.dto.businessprofile.BusinessServiceResponseDTO;
import com.smartroad.services.domain.entity.BusinessServiceEntity;
import org.springframework.stereotype.Component;

/**
 * Component for converting between {@link BusinessServiceEntity} and business service DTOs.
 */
@Component
public class BusinessServiceMapper {

    /**
     * Converts a {@link BusinessServiceEntity} to a {@link BusinessServiceResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    public BusinessServiceResponseDTO toResponseDTO(BusinessServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new BusinessServiceResponseDTO(
            entity.getId(),
            entity.getBusinessProfileId(),
            entity.getServiceName()
        );
    }

    /**
     * Converts a {@link BusinessServiceRequestDTO} to a {@link BusinessServiceEntity}.
     *
     * @param dto the source request DTO
     * @return the mapped entity
     */
    public BusinessServiceEntity toEntity(BusinessServiceRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return BusinessServiceEntity.builder()
            .businessProfileId(dto.businessProfileId())
            .serviceName(dto.serviceName())
            .build();
    }

    /**
     * Updates an existing {@link BusinessServiceEntity} from a {@link BusinessServiceRequestDTO}.
     *
     * @param dto the source request DTO
     * @param entity the target entity to update
     */
    public void updateEntityFromDTO(BusinessServiceRequestDTO dto, BusinessServiceEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setServiceName(dto.serviceName());
    }
}

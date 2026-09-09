package com.smartroad.services.core.mapper.company;

import com.smartroad.services.core.dto.company.CompanyServiceResponseDTO;
import com.smartroad.services.domain.entity.company.CompanyServiceEntity;
import org.springframework.stereotype.Component;

/**
 * Component for converting between {@link CompanyServiceEntity} and company service DTOs.
 */
@Component
public class CompanyServiceMapper {

    /**
     * Converts a {@link CompanyServiceEntity} to a {@link CompanyServiceResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    public CompanyServiceResponseDTO toResponseDTO(CompanyServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CompanyServiceResponseDTO(
            entity.getId(),
            entity.getServiceName(),
            entity.getDisplayOrder()
        );
    }
}

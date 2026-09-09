package com.smartroad.services.core.mapper.company;

import com.smartroad.services.core.dto.company.CompanyProfileResponseDTO;
import com.smartroad.services.domain.entity.company.CompanyProfileEntity;
import org.springframework.stereotype.Component;

/**
 * Component for converting between {@link CompanyProfileEntity} and company profile DTOs.
 */
@Component
public class CompanyProfileMapper {

    /**
     * Converts a {@link CompanyProfileEntity} to a {@link CompanyProfileResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    public CompanyProfileResponseDTO toResponseDTO(CompanyProfileEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CompanyProfileResponseDTO(
            entity.getId(),
            entity.getBrandName(),
            entity.getBusinessName(),
            entity.getBusinessType(),
            entity.getProductName(),
            entity.getTagline(),
            entity.getPhone(),
            entity.getAddress()
        );
    }
}

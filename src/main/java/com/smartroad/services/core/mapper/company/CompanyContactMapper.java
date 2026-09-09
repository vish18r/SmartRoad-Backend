package com.smartroad.services.core.mapper.company;

import com.smartroad.services.core.dto.company.CompanyContactResponseDTO;
import com.smartroad.services.domain.entity.company.CompanyContactEntity;
import org.springframework.stereotype.Component;

/**
 * Component for converting between {@link CompanyContactEntity} and company contact DTOs.
 */
@Component
public class CompanyContactMapper {

    /**
     * Converts a {@link CompanyContactEntity} to a {@link CompanyContactResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    public CompanyContactResponseDTO toResponseDTO(CompanyContactEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CompanyContactResponseDTO(
            entity.getId(),
            entity.getContactName(),
            entity.getContactRole(),
            entity.getPhoneNumber1(),
            entity.getPhoneNumber2()
        );
    }
}

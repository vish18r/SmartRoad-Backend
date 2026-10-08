package com.smartroad.services.core.mapper.shift;

import com.smartroad.services.core.dto.shift.ShiftRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftResponseDTO;
import com.smartroad.services.domain.entity.shift.ShiftTemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between {@link ShiftTemplateEntity} and shift template DTOs.
 *
 * @author Vishal
 * @version 1.0
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShiftMapper {

    /**
     * Converts a {@link ShiftTemplateEntity} to a {@link ShiftResponseDTO}. The assigned-workers
     * count is not a stored field and is left null here; the service fills it in separately when
     * the caller needs it.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    @Mapping(target = "assignedWorkersCount", ignore = true)
    ShiftResponseDTO toShiftResponseDTO(ShiftTemplateEntity entity);

    /**
     * Converts a {@link ShiftRequestDTO} to a new {@link ShiftTemplateEntity}. The computed
     * {@code totalWorkingMinutes} field is always derived by the service, never by this mapper.
     *
     * @param dto the source DTO
     * @return the mapped entity
     */
    @Mapping(target = "totalWorkingMinutes", ignore = true)
    ShiftTemplateEntity toShiftEntity(ShiftRequestDTO dto);

    /**
     * Updates a {@link ShiftTemplateEntity} with data from a {@link ShiftRequestDTO}. The
     * computed {@code totalWorkingMinutes} field is always derived by the service, never by this
     * mapper, and {@code organizationId} is never changed by an update: a shift template's owning
     * organization is fixed at creation.
     *
     * @param dto the source DTO
     * @param entity the target entity to update
     */
    @Mapping(target = "totalWorkingMinutes", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateShiftEntityFromDto(ShiftRequestDTO dto, @MappingTarget ShiftTemplateEntity entity);
}

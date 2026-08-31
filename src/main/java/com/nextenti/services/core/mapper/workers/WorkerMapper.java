package com.nextenti.services.core.mapper.workers;

import com.nextenti.services.core.dto.workers.WorkerRequestDTO;
import com.nextenti.services.core.dto.workers.WorkerResponseDTO;
import com.nextenti.services.domain.entity.WorkerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for converting between {@link WorkerEntity} and worker DTOs.
 *
 * @author Vishal
 * @version 1.0
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WorkerMapper {

    /**
     * Converts a {@link WorkerEntity} to a {@link WorkerResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    WorkerResponseDTO toWorkerResponseDTO(WorkerEntity entity);

    /**
     * Converts a {@link WorkerRequestDTO} to a {@link WorkerEntity}.
     *
     * @param dto the source DTO
     * @return the mapped entity
     */
    WorkerEntity toWorkerEntity(WorkerRequestDTO dto);

    /**
     * Updates a {@link WorkerEntity} with data from a {@link WorkerRequestDTO}.
     *
     * @param dto the source DTO
     * @param entity the target entity to update
     */
    void updateWorkerEntityFromDto(WorkerRequestDTO dto, @MappingTarget WorkerEntity entity);
}

package com.smartroad.services.core.mapper.workers;

import com.smartroad.services.core.dto.workers.WorkerRequestDTO;
import com.smartroad.services.core.dto.workers.WorkerResponseDTO;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
    @Mapping(target = "faceStatus", expression = "java(entity.getFaceDescriptor() != null && !entity.getFaceDescriptor().isBlank() ? com.smartroad.services.common.enums.workers.WorkerFaceStatusEnum.FACE_REGISTERED : com.smartroad.services.common.enums.workers.WorkerFaceStatusEnum.FACE_NOT_REGISTERED)")
    @Mapping(target = "fullName", expression = "java(((entity.getFirstName() == null ? \"\" : entity.getFirstName()) + \" \" + (entity.getLastName() == null ? \"\" : entity.getLastName())).trim())")
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
    @Mapping(target = "faceDescriptor", ignore = true)
    void updateWorkerEntityFromDto(WorkerRequestDTO dto, @MappingTarget WorkerEntity entity);
}

package com.smartroad.services.core.mapper.auth;

import com.smartroad.services.core.dto.auth.UserResponseDTO;
import com.smartroad.services.domain.entity.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between User entity and UserResponseDTO DTO.
 *
 * @author
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts User entity to UserResponseDTO.
     *
     * @param user the UserEntity
     * @return the UserResponseDTO
     */
    @Mapping(source = "emailId", target = "email")
    @Mapping(source = "emailVerifiedYn", target = "emailVerified")
    UserResponseDTO toResponse(UserEntity user);
}

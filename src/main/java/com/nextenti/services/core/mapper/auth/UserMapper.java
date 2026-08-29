package com.nextenti.services.core.mapper.auth;

import com.nextenti.services.core.dto.auth.UserResponse;
import com.nextenti.services.domain.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between User entity and UserResponse DTO.
 *
 * @author
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts User entity to UserResponse DTO.
     *
     * @param user the User entity
     * @return the UserResponse DTO
     */
    @Mapping(source = "emailId", target = "email")
    UserResponse toResponse(User user);
}

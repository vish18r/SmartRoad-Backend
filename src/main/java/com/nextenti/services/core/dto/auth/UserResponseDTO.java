package com.nextenti.services.core.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextenti.services.common.enums.auth.UserRoleEnum;
import com.nextenti.services.common.enums.auth.UserStatusEnum;
import com.nextenti.services.common.enums.auth.UserTypeEnum;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String countryCode;
    private UserRoleEnum role;
    private UserTypeEnum userType;
    private UserStatusEnum status;
    private Boolean emailVerified;

}

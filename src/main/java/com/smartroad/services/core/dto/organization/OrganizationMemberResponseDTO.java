package com.smartroad.services.core.dto.organization;

import com.smartroad.services.common.enums.UserRole;
import java.util.UUID;

public record OrganizationMemberResponseDTO(UUID userId, UserRole role, boolean active) { }

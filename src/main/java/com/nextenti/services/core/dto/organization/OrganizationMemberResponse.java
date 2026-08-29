package com.nextenti.services.core.dto.organization;

import com.nextenti.services.common.enums.UserRole;
import java.util.UUID;

public record OrganizationMemberResponse(UUID userId, UserRole role, boolean active) { }

package com.smartroad.services.core.dto.organization;

import com.smartroad.services.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrganizationMemberRequestDTO(@NotNull UUID userId, @NotNull UserRole role) { }

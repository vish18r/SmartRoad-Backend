package com.nextenti.services.core.dto.organization;

import com.nextenti.services.common.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrganizationMemberRequest(@NotNull UUID userId, @NotNull UserRole role) { }

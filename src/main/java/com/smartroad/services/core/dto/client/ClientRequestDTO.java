package com.smartroad.services.core.dto.client;
import jakarta.validation.constraints.*;
public record ClientRequestDTO(@NotNull java.util.UUID organizationId, @NotBlank @Size(max=255) String name,
                            @Size(max=255) String contactPerson, @Email String email, @Size(max=32) String phoneNumber,
                            @Size(max=32) String gstNumber, String address) { }

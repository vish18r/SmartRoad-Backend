package com.nextenti.services.core.dto.client;
import jakarta.validation.constraints.*;
public record ClientRequest(@NotNull java.util.UUID organizationId, @NotBlank @Size(max=255) String name,
                            @Size(max=255) String contactPerson, @Email String email, @Size(max=32) String phoneNumber,
                            @Size(max=32) String gstNumber, String address) { }

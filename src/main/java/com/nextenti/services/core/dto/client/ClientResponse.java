package com.nextenti.services.core.dto.client;
import java.util.UUID;
public record ClientResponse(UUID id, UUID organizationId, String name, String contactPerson, String email,
                             String phoneNumber, String gstNumber, String address, boolean active) { }

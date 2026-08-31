package com.nextenti.services.core.dto.workers;

import com.nextenti.services.common.enums.workers.WorkerStatusEnum;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for worker data returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
public record WorkerResponseDTO(
    UUID id,
    UUID organizationId,
    String firstName,
    String lastName,
    String emailId,
    String phoneNumber,
    WorkerStatusEnum status,
    String role,
    LocalDate joiningDate,
    UUID assignedSiteId,
    String address,
    String city,
    String state,
    String postalCode,
    String country,
    String aadharNumber,
    String bankAccountNumber,
    String bankName,
    String ifscCode,
    String emergencyContactName,
    String emergencyContactPhone,
    Integer experienceYears
) {}

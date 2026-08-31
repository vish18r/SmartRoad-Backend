package com.nextenti.services.core.dto.workers;

import com.nextenti.services.common.enums.workers.WorkerStatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a worker.
 *
 * @author Vishal
 * @version 1.0
 */
public record WorkerRequestDTO(
    @NotNull(message = "{organization.id.required}")
    UUID organizationId,

    @NotBlank(message = "{first.name.required}")
    String firstName,

    String lastName,

    @Email(message = "{email.invalid}")
    String emailId,

    @Pattern(regexp = "^\\+?[0-9]{10,}$", message = "{phone.invalid}")
    String phoneNumber,

    @NotNull(message = "{status.required}")
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

package com.nextenti.services.core.dto.vendor;

import com.nextenti.services.common.enums.vendor.VendorTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating/updating vendors.
 *
 * @author Vishal
 * @version 1.0
 */
public record VendorRequestDTO(

    @NotBlank(message = "{vendor.name.required}")
    String vendorName,

    String contactPerson,

    String phone,

    @Email(message = "{email.invalid}")
    String email,

    String address,

    String city,

    String state,

    String postalCode,

    String gstNumber,

    VendorTypeEnum vendorType,

    String paymentTerms,

    @NotNull(message = "{vendor.active.required}")
    Boolean isActive
) {}

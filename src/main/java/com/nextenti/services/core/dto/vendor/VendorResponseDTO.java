package com.nextenti.services.core.dto.vendor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nextenti.services.common.enums.vendor.VendorTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for vendor data returned to API consumers.
 *
 * @author Vishal
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VendorResponseDTO {

    private UUID id;

    private UUID organizationId;

    private String vendorName;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    private String city;

    private String state;

    private String postalCode;

    private String gstNumber;

    private VendorTypeEnum vendorType;

    private String paymentTerms;

    private Boolean isActive;

    private UUID createdBy;

    private UUID modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}

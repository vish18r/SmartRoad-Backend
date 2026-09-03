package com.smartroad.services.core.dto.material;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 * Response DTO for material data returned to API consumers.
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
public class MaterialResponseDTO {

    private UUID id;

    private UUID organizationId;

    private String materialCode;

    private String materialName;

    private String unit;

    private String category;

    private String description;

    private UUID createdBy;

    private UUID modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}

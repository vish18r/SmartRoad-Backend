package com.smartroad.services.api.rest.company;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.company.CompleteCompanyProfileResponseDTO;
import com.smartroad.services.core.service.company.CompanyProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the public, unauthenticated company profile API.
 * Delegates all business logic to {@link CompanyProfileService}. This
 * controller intentionally carries no {@code @SecurityRequirement}, matching
 * the convention used for other public endpoints (e.g. auth signup/login).
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/public/company-profile")
public class CompanyProfileController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyProfileController.class);

    private final CompanyProfileService companyProfileService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param companyProfileService the company profile service
     */
    public CompanyProfileController(CompanyProfileService companyProfileService) {
        this.companyProfileService = companyProfileService;
    }

    /**
     * Retrieves the complete public company profile: brand details, founder,
     * additional contacts, and services.
     *
     * @return {@link ResponseEntity} containing the {@link CompleteCompanyProfileResponseDTO}
     * @throws SmartRoadException if the company profile has not been seeded
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Object> getCompanyProfile() throws SmartRoadException {
        logger.info("--Inside getCompanyProfile method--");

        CompleteCompanyProfileResponseDTO response = companyProfileService.getPublicCompanyProfile();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

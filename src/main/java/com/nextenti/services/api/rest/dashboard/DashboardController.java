package com.nextenti.services.api.rest.dashboard;

import com.nextenti.services.api.utils.RequestUtil;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.dashboard.DashboardStatsDTO;
import com.nextenti.services.core.service.dashboard.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for dashboard operations.
 * Provides statistics and overview data for the dashboard UI.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param dashboardService the dashboard service
     */
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Retrieves dashboard statistics for an organization.
     *
     * @param organizationId the UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link DashboardStatsDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDashboardStats(@RequestParam UUID organizationId,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getDashboardStats method--");

        UUID userId = RequestUtil.extractUserId();
        DashboardStatsDTO response = dashboardService.getDashboardStats(organizationId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

package com.smartroad.services.api.rest.dashboard;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.dashboard.DashboardResponseDTO;
import com.smartroad.services.core.dto.dashboard.DashboardStatsDTO;
import com.smartroad.services.core.dto.dashboard.OwnerDashboardResponseDTO;
import com.smartroad.services.core.dto.dashboard.SiteDashboardResponseDTO;
import com.smartroad.services.core.service.dashboard.DashboardService;
import com.smartroad.services.core.service.dashboard.OwnerDashboardService;
import com.smartroad.services.core.service.dashboard.SiteDashboardService;
import com.smartroad.services.core.service.organization.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
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
    private final OwnerDashboardService ownerDashboardService;
    private final SiteDashboardService siteDashboardService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param dashboardService the dashboard service
     * @param ownerDashboardService the owner dashboard service
     * @param siteDashboardService the site dashboard service
     * @param organizationService the organization service, used to resolve the caller's organization
     */
    public DashboardController(DashboardService dashboardService,
                               OwnerDashboardService ownerDashboardService,
                               SiteDashboardService siteDashboardService,
                               OrganizationService organizationService) {
        this.dashboardService = dashboardService;
        this.ownerDashboardService = ownerDashboardService;
        this.siteDashboardService = siteDashboardService;
        this.organizationService = organizationService;
    }

    /**
     * Retrieves the aggregate dashboard payload — statistics plus a recent activity feed.
     * The organization is optional; when omitted it is resolved from the caller's membership.
     *
     * @param organizationId the optional UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link DashboardResponseDTO}
     * @throws SmartRoadException if the caller belongs to no organization or retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDashboard(@RequestParam(required = false) UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getDashboard method--");

        DashboardResponseDTO response = dashboardService.getDashboard(resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves dashboard statistics for an organization.
     * The organization is optional; when omitted it is resolved from the caller's membership.
     *
     * @param organizationId the optional UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link DashboardStatsDTO}
     * @throws SmartRoadException if the caller belongs to no organization or retrieval fails
     */
    @GetMapping(path = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDashboardStats(@RequestParam(required = false) UUID organizationId,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getDashboardStats method--");

        DashboardStatsDTO response = dashboardService.getDashboardStats(resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves the project-focused dashboard statistics for an organization.
     * The organization is optional; when omitted it is resolved from the caller's membership.
     *
     * @param organizationId the optional UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link DashboardStatsDTO}
     * @throws SmartRoadException if the caller belongs to no organization or retrieval fails
     */
    @GetMapping(path = "/projects", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getProjectDashboard(@RequestParam(required = false) UUID organizationId,
                                                       @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getProjectDashboard method--");

        DashboardStatsDTO response = dashboardService.getDashboardStats(resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves the executive owner dashboard for an organization, covering
     * portfolio, contract, financial, and deadline metrics.
     * The organization is optional; when omitted it is resolved from the caller's membership.
     *
     * @param organizationId the optional UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link OwnerDashboardResponseDTO}
     * @throws SmartRoadException if the caller belongs to no organization or retrieval fails
     */
    @GetMapping(path = "/owner", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getOwnerDashboard(@RequestParam(required = false) UUID organizationId,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getOwnerDashboard method--");

        OwnerDashboardResponseDTO response = ownerDashboardService.getOwnerDashboard(resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves the supervisor's site dashboard for one project on one day: physical
     * progress, the workforce on site, material stock held there, and the day's photos.
     * The date is optional and defaults to today.
     *
     * @param projectId the UUID of the project (site) to report on
     * @param date the day to report on in ISO form (yyyy-MM-dd); defaults to today
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link SiteDashboardResponseDTO}
     * @throws SmartRoadException if the project is not found or the caller is not authorized
     */
    @GetMapping(path = "/site", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getSiteDashboard(@RequestParam UUID projectId,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getSiteDashboard method--");

        UUID userId = RequestUtil.extractUserId();
        SiteDashboardResponseDTO response = siteDashboardService.getSiteDashboard(userId, projectId,
                date == null ? LocalDate.now() : date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns the supplied organization, or resolves the caller's own organization when none was supplied.
     *
     * @param organizationId the organization UUID from the request, possibly null
     * @return the organization UUID to report on
     * @throws SmartRoadException if no organization was supplied and the caller belongs to none
     */
    private UUID resolveOrganization(UUID organizationId) throws SmartRoadException {
        if (organizationId != null) {
            return organizationId;
        }
        return organizationService.resolveDefaultOrganizationId(RequestUtil.extractUserId());
    }
}

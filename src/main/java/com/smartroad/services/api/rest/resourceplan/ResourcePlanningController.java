package com.smartroad.services.api.rest.resourceplan;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum;
import com.smartroad.services.common.enums.resourceplan.ResourceTypeEnum;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.resourceplan.ResourceAllocationRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceAllocationResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourceAvailabilityResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanDetailResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanPageResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanResponseDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanSummaryDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanValidateRequestDTO;
import com.smartroad.services.core.dto.resourceplan.ResourcePlanValidationResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.resourceplan.ResourcePlanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * REST controller for resource planning: plans and their resource allocations, conflict
 * validation, availability lookups, and summary figures. Handles HTTP concerns only, delegating
 * all business logic to {@link ResourcePlanService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/resource-planning")
public class ResourcePlanningController {

    private static final Logger logger = LoggerFactory.getLogger(ResourcePlanningController.class);

    private final ResourcePlanService resourcePlanService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param resourcePlanService the resource plan service
     * @param organizationService the organization service, used to resolve the caller's organization
     */
    public ResourcePlanningController(ResourcePlanService resourcePlanService, OrganizationService organizationService) {
        this.resourcePlanService = resourcePlanService;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new resource plan.
     *
     * @param request the plan creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPlan(@RequestBody @Valid ResourcePlanRequestDTO request,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createPlan method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanResponseDTO response = resourcePlanService.createPlan(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists a page of plans matching the optional filters.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param projectId the optional project filter
     * @param dateFrom the optional inclusive start of the plan-date window
     * @param dateTo the optional inclusive end of the plan-date window
     * @param status the optional status filter
     * @param page the optional one-based page number
     * @param limit the optional page size
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ResourcePlanPageResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listPlans(@RequestParam(required = false) UUID organizationId,
                                            @RequestParam(required = false) UUID projectId,
                                            @RequestParam(required = false) LocalDate dateFrom,
                                            @RequestParam(required = false) LocalDate dateTo,
                                            @RequestParam(required = false) ResourcePlanStatusEnum status,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer limit,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listPlans method--");

        UUID userId = RequestUtil.extractUserId();
        int resolvedPage = page != null ? page : NextentiConstants.DEFAULT_PAGE;
        int resolvedLimit = limit != null ? limit : NextentiConstants.DEFAULT_LIMIT;
        ResourcePlanPageResponseDTO response = resourcePlanService.listPlans(userId, resolveOrganization(organizationId),
                projectId, dateFrom, dateTo, status, resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a plan with its resolved allocations.
     *
     * @param id the plan UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ResourcePlanDetailResponseDTO}
     * @throws SmartRoadException if the plan is not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getPlan(@PathVariable UUID id,
                                          @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getPlan method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanDetailResponseDTO response = resourcePlanService.getPlan(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a plan's details.
     *
     * @param id the plan UUID
     * @param request the plan update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if update fails or the plan is not editable
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updatePlan(@PathVariable UUID id,
                                             @RequestBody @Valid ResourcePlanRequestDTO request,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updatePlan method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanResponseDTO response = resourcePlanService.updatePlan(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Cancels a plan. Provided for REST convention compliance; behaves identically to
     * {@link #cancelPlan}.
     *
     * @param id the plan UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the cancelled {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if the plan is not found
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deletePlan(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deletePlan method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanResponseDTO response = resourcePlanService.cancelPlan(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Confirms a plan. Validates the plan's current allocations first; if any blocking conflict
     * remains, the plan is left unchanged and a 409 is returned with the conflict list instead of
     * confirming.
     *
     * @param id the plan UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the confirmed {@link ResourcePlanResponseDTO} on
     *         success, or a 409 {@link ResourcePlanValidationResponseDTO} when blocking conflicts remain
     * @throws SmartRoadException if the plan is not found or the transition is not allowed
     */
    @PostMapping(path = "/{id}/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> confirmPlan(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside confirmPlan method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanValidationResponseDTO validation = resourcePlanService.validatePlan(userId, id);
        if (validation.hasBlockingConflicts()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(validation);
        }

        ResourcePlanResponseDTO response = resourcePlanService.confirmPlan(userId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Cancels a plan from any non-terminal status.
     *
     * @param id the plan UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the cancelled {@link ResourcePlanResponseDTO}
     * @throws SmartRoadException if the plan is not found
     */
    @PostMapping(path = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> cancelPlan(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside cancelPlan method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanResponseDTO response = resourcePlanService.cancelPlan(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Computes resource-planning summary figures for a project (or organization-wide) on a date.
     *
     * @param organizationId the optional organization UUID, resolved from the caller when omitted
     * @param projectId the optional project filter
     * @param date the date to summarize
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ResourcePlanSummaryDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getSummary(@RequestParam(required = false) UUID organizationId,
                                             @RequestParam(required = false) UUID projectId,
                                             @RequestParam LocalDate date,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getSummary method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanSummaryDTO response = resourcePlanService.getSummary(userId, resolveOrganization(organizationId),
                projectId, date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Adds a new allocation to a plan.
     *
     * @param planId the plan UUID
     * @param request the allocation creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ResourceAllocationResponseDTO}
     * @throws SmartRoadException if the plan is not found or not editable
     */
    @PostMapping(path = "/{planId}/allocations", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> addAllocation(@PathVariable UUID planId,
                                                @RequestBody @Valid ResourceAllocationRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside addAllocation method--");

        UUID userId = RequestUtil.extractUserId();
        ResourceAllocationResponseDTO response = resourcePlanService.addAllocation(userId, planId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an allocation on a plan.
     *
     * @param planId the plan UUID
     * @param allocationId the allocation UUID
     * @param request the allocation update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ResourceAllocationResponseDTO}
     * @throws SmartRoadException if the plan or allocation is not found, or the plan is not editable
     */
    @PutMapping(path = "/{planId}/allocations/{allocationId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateAllocation(@PathVariable UUID planId, @PathVariable UUID allocationId,
                                                   @RequestBody @Valid ResourceAllocationRequestDTO request,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateAllocation method--");

        UUID userId = RequestUtil.extractUserId();
        ResourceAllocationResponseDTO response = resourcePlanService.updateAllocation(userId, planId, allocationId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Removes an allocation from a plan.
     *
     * @param planId the plan UUID
     * @param allocationId the allocation UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if the plan or allocation is not found, or the plan is not editable
     */
    @DeleteMapping(path = "/{planId}/allocations/{allocationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAllocation(@PathVariable UUID planId, @PathVariable UUID allocationId,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside removeAllocation method--");

        UUID userId = RequestUtil.extractUserId();
        resourcePlanService.removeAllocation(userId, planId, allocationId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Validates a candidate set of allocations without persisting anything.
     *
     * @param request the validation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ResourcePlanValidationResponseDTO}
     * @throws SmartRoadException if the project is not found
     */
    @PostMapping(path = "/validate", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> validate(@RequestBody @Valid ResourcePlanValidateRequestDTO request,
                                           @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside validate method--");

        UUID userId = RequestUtil.extractUserId();
        ResourcePlanValidationResponseDTO response = resourcePlanService.validatePlanRequest(userId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Lists available resources for a project/date/time window, optionally filtered by resource type.
     *
     * @param projectId the project UUID
     * @param date the date to check availability for
     * @param startTime the optional window start time
     * @param endTime the optional window end time
     * @param resourceType the optional resource type filter
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ResourceAvailabilityResponseDTO}
     * @throws SmartRoadException if the project is not found
     */
    @GetMapping(path = "/availability", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getAvailability(@RequestParam UUID projectId,
                                                  @RequestParam LocalDate date,
                                                  @RequestParam(required = false) LocalTime startTime,
                                                  @RequestParam(required = false) LocalTime endTime,
                                                  @RequestParam(required = false) ResourceTypeEnum resourceType,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getAvailability method--");

        UUID userId = RequestUtil.extractUserId();
        ResourceAvailabilityResponseDTO response = resourcePlanService.getAvailability(userId, projectId, date,
                startTime, endTime, resourceType);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns the supplied organization, or resolves the caller's own organization when none was supplied.
     *
     * @param organizationId the organization UUID from the request, possibly null
     * @return the organization UUID to operate on
     * @throws SmartRoadException if no organization was supplied and the caller belongs to none
     */
    private UUID resolveOrganization(UUID organizationId) throws SmartRoadException {
        if (organizationId != null) {
            return organizationId;
        }
        return organizationService.resolveDefaultOrganizationId(RequestUtil.extractUserId());
    }
}

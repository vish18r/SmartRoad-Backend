package com.smartroad.services.api.rest.shift;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.enums.shift.ShiftAssignmentStatusEnum;
import com.smartroad.services.common.enums.shift.ShiftStatusEnum;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.shift.ShiftAssignmentPageResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftAssignmentRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftAssignmentResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftPageResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftResponseDTO;
import com.smartroad.services.core.dto.shift.ShiftStatusUpdateRequestDTO;
import com.smartroad.services.core.dto.shift.ShiftSummaryResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.shift.ShiftAssignmentService;
import com.smartroad.services.core.service.shift.ShiftService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for shift management: shift template CRUD and status changes, plus worker
 * shift assignment CRUD. Handles HTTP requests and delegates all business logic to
 * {@link ShiftService} and {@link ShiftAssignmentService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftController.class);

    private final ShiftService shiftService;
    private final ShiftAssignmentService shiftAssignmentService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param shiftService the shift template service
     * @param shiftAssignmentService the worker shift assignment service
     * @param organizationService the organization service, used to resolve the caller's organization
     */
    public ShiftController(ShiftService shiftService, ShiftAssignmentService shiftAssignmentService,
                           OrganizationService organizationService) {
        this.shiftService = shiftService;
        this.shiftAssignmentService = shiftAssignmentService;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new shift template.
     *
     * @param request the shift creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ShiftResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createShift(@RequestBody @Valid ShiftRequestDTO request,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createShift method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftResponseDTO response = shiftService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a page of shift templates matching the optional filters.
     * Pagination is read from the x-page and x-limit headers the web client sends, falling back
     * to the page and limit request parameters.
     *
     * @param organizationId the optional organization UUID; resolved from the caller when omitted
     * @param status the optional shift status filter
     * @param search the optional free-text search term, as a query parameter
     * @param searchHeader the optional free-text search term, as the x-search header
     * @param page the optional one-based page number, as a query parameter
     * @param limit the optional page size, as a query parameter
     * @param pageHeader the optional page number, as the x-page header
     * @param limitHeader the optional page size, as the x-limit header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ShiftPageResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listShifts(@RequestParam(required = false) UUID organizationId,
                                             @RequestParam(required = false) ShiftStatusEnum status,
                                             @RequestParam(required = false) String search,
                                             @RequestHeader(name = NextentiConstants.HEADER_SEARCH, required = false) String searchHeader,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer limit,
                                             @RequestHeader(name = NextentiConstants.HEADER_PAGE, required = false) Integer pageHeader,
                                             @RequestHeader(name = NextentiConstants.HEADER_LIMIT, required = false) Integer limitHeader,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listShifts method--");

        UUID userId = RequestUtil.extractUserId();
        String term = search != null ? search : searchHeader;
        int resolvedPage = firstNonNull(page, pageHeader, NextentiConstants.DEFAULT_PAGE);
        int resolvedLimit = firstNonNull(limit, limitHeader, NextentiConstants.DEFAULT_LIMIT);

        ShiftPageResponseDTO response = shiftService.list(userId, resolveOrganization(organizationId), status, term,
                resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves aggregated shift management figures for the dashboard summary cards.
     *
     * @param organizationId the optional organization UUID; resolved from the caller when omitted
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ShiftSummaryResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @GetMapping(path = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getSummary(@RequestParam(required = false) UUID organizationId,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getSummary method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftSummaryResponseDTO response = shiftService.summary(userId, resolveOrganization(organizationId));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves one shift template's full details.
     *
     * @param id the shift template UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ShiftResponseDTO}
     * @throws SmartRoadException if the shift is not found or the caller is not authorized
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getShift(@PathVariable UUID id,
                                           @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getShift method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftResponseDTO response = shiftService.getById(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a shift template.
     *
     * @param id the shift template UUID
     * @param request the shift update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ShiftResponseDTO}
     * @throws SmartRoadException if the shift is not found, the caller is not authorized, the
     *                            code is already in use, or the times/break duration are invalid
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateShift(@PathVariable UUID id,
                                              @RequestBody @Valid ShiftRequestDTO request,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateShift method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftResponseDTO response = shiftService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Moves a shift template to a new lifecycle status (activate/deactivate) without touching
     * its other fields.
     *
     * @param id the shift template UUID
     * @param request the status change request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ShiftResponseDTO}
     * @throws SmartRoadException if the shift is not found or the caller is not authorized
     */
    @PatchMapping(path = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateShiftStatus(@PathVariable UUID id,
                                                    @RequestBody @Valid ShiftStatusUpdateRequestDTO request,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateShiftStatus method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftResponseDTO response = shiftService.updateStatus(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a shift template. A shift template with assignment history cannot be deleted.
     *
     * @param id the shift template UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK
     * @throws SmartRoadException if the shift is not found, the caller is not authorized, or the
     *                            shift still has assignment history
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteShift(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteShift method--");

        UUID userId = RequestUtil.extractUserId();
        shiftService.delete(userId, id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Creates a new worker shift assignment.
     *
     * @param request the assignment creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the worker or shift is not found, not active, the caller is
     *                            not authorized, the date range is invalid, or it overlaps an
     *                            existing active assignment
     */
    @PostMapping(path = "/assignments", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createAssignment(@RequestBody @Valid ShiftAssignmentRequestDTO request,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createAssignment method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftAssignmentResponseDTO response = shiftAssignmentService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a page of worker shift assignments matching the optional filters.
     * Pagination is read from the x-page and x-limit headers the web client sends, falling back
     * to the page and limit request parameters.
     *
     * @param organizationId the optional organization UUID; resolved from the caller when omitted
     * @param workerId the optional worker UUID filter
     * @param shiftId the optional shift UUID filter
     * @param status the optional assignment status filter
     * @param effectiveDate the optional date the assignment's range must cover
     * @param page the optional one-based page number, as a query parameter
     * @param limit the optional page size, as a query parameter
     * @param pageHeader the optional page number, as the x-page header
     * @param limitHeader the optional page size, as the x-limit header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ShiftAssignmentPageResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @GetMapping(path = "/assignments", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listAssignments(@RequestParam(required = false) UUID organizationId,
                                                  @RequestParam(required = false) UUID workerId,
                                                  @RequestParam(required = false) UUID shiftId,
                                                  @RequestParam(required = false) ShiftAssignmentStatusEnum status,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate,
                                                  @RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestHeader(name = NextentiConstants.HEADER_PAGE, required = false) Integer pageHeader,
                                                  @RequestHeader(name = NextentiConstants.HEADER_LIMIT, required = false) Integer limitHeader,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listAssignments method--");

        UUID userId = RequestUtil.extractUserId();
        int resolvedPage = firstNonNull(page, pageHeader, NextentiConstants.DEFAULT_PAGE);
        int resolvedLimit = firstNonNull(limit, limitHeader, NextentiConstants.DEFAULT_LIMIT);

        ShiftAssignmentPageResponseDTO response = shiftAssignmentService.list(userId, resolveOrganization(organizationId),
                workerId, shiftId, status, effectiveDate, resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves one assignment's full details.
     *
     * @param id the assignment UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the assignment is not found or the caller is not authorized
     */
    @GetMapping(path = "/assignments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getAssignment(@PathVariable UUID id,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getAssignment method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftAssignmentResponseDTO response = shiftAssignmentService.getById(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a worker shift assignment's shift, effective date range or status.
     *
     * @param id the assignment UUID
     * @param request the assignment update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the assignment, worker or shift is not found, not active, the
     *                            caller is not authorized, the date range is invalid, or it
     *                            overlaps another active assignment
     */
    @PutMapping(path = "/assignments/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateAssignment(@PathVariable UUID id,
                                                   @RequestBody @Valid ShiftAssignmentRequestDTO request,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateAssignment method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftAssignmentResponseDTO response = shiftAssignmentService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Ends a worker shift assignment. The row is never deleted: it is closed out by setting its
     * effective-to date and moving its status to ENDED, preserving the full assignment history.
     *
     * @param id the assignment UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the assignment is not found or the caller is not authorized
     */
    @DeleteMapping(path = "/assignments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> endAssignment(@PathVariable UUID id,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside endAssignment method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftAssignmentResponseDTO response = shiftAssignmentService.end(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Returns the first of the supplied pagination values that is present.
     *
     * @param parameter the value from the request parameter, possibly null
     * @param header the value from the request header, possibly null
     * @param fallback the default value
     * @return the first non-null value, or the fallback
     */
    private int firstNonNull(Integer parameter, Integer header, int fallback) {
        if (parameter != null) {
            return parameter;
        }
        return header != null ? header : fallback;
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

package com.smartroad.services.api.rest.payroll;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.enums.payroll.PayrollStatusEnum;
import com.smartroad.services.common.enums.workers.WageTypeEnum;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.payroll.PayrollCalculateRequestDTO;
import com.smartroad.services.core.dto.payroll.PayrollPageResponseDTO;
import com.smartroad.services.core.dto.payroll.PayrollRequestDTO;
import com.smartroad.services.core.dto.payroll.PayrollResponseDTO;
import com.smartroad.services.core.dto.payroll.PayrollSummaryResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.core.service.payroll.PayrollService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.List;
import java.util.UUID;

/**
 * REST controller for the payroll module: calculation previews, payroll CRUD and the
 * DRAFT &rarr; PROCESSED &rarr; APPROVED &rarr; PAID status workflow.
 * Handles HTTP requests and delegates all business logic to {@link PayrollService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

    private static final Logger logger = LoggerFactory.getLogger(PayrollController.class);

    private final PayrollService payrollService;
    private final OrganizationService organizationService;

    /**
     * Constructs the controller with required service dependencies.
     *
     * @param payrollService the payroll service
     * @param organizationService the organization service, used to resolve the caller's organization
     */
    public PayrollController(PayrollService payrollService, OrganizationService organizationService) {
        this.payrollService = payrollService;
        this.organizationService = organizationService;
    }

    /**
     * Calculates a payroll preview for a worker and period. Nothing is persisted.
     *
     * @param request the calculation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the computed {@link PayrollResponseDTO}
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized, the period is
     *                            invalid, or the wage configuration is incomplete
     */
    @PostMapping(path = "/calculate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> calculate(@RequestBody @Valid PayrollCalculateRequestDTO request,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside calculate method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.calculate(userId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Creates and persists a new payroll in DRAFT status.
     *
     * @param request the payroll creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link PayrollResponseDTO}
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized, the period is
     *                            invalid or overlaps an existing payroll, or the wage configuration
     *                            is incomplete
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createPayroll(@RequestBody @Valid PayrollRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createPayroll method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a page of payrolls matching the optional filters, most recent period first.
     * Pagination is read from the x-page and x-limit headers the web client sends, falling back
     * to the page and limit request parameters.
     *
     * @param organizationId the optional organization UUID; resolved from the caller when omitted
     * @param workerId the optional worker UUID filter
     * @param employeeId the optional employee ID filter
     * @param status the optional payroll status filter
     * @param wageType the optional wage type filter
     * @param startDate the optional period-overlap start filter
     * @param endDate the optional period-overlap end filter
     * @param search the optional free-text search term, as a query parameter
     * @param searchHeader the optional free-text search term, as the x-search header
     * @param page the optional one-based page number, as a query parameter
     * @param limit the optional page size, as a query parameter
     * @param pageHeader the optional page number, as the x-page header
     * @param limitHeader the optional page size, as the x-limit header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link PayrollPageResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listPayrolls(@RequestParam(required = false) UUID organizationId,
                                               @RequestParam(required = false) UUID workerId,
                                               @RequestParam(required = false) String employeeId,
                                               @RequestParam(required = false) PayrollStatusEnum status,
                                               @RequestParam(required = false) WageTypeEnum wageType,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                               @RequestParam(required = false) String search,
                                               @RequestHeader(name = NextentiConstants.HEADER_SEARCH, required = false) String searchHeader,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer limit,
                                               @RequestHeader(name = NextentiConstants.HEADER_PAGE, required = false) Integer pageHeader,
                                               @RequestHeader(name = NextentiConstants.HEADER_LIMIT, required = false) Integer limitHeader,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listPayrolls method--");

        UUID userId = RequestUtil.extractUserId();
        String term = search != null ? search : searchHeader;
        int resolvedPage = firstNonNull(page, pageHeader, NextentiConstants.DEFAULT_PAGE);
        int resolvedLimit = firstNonNull(limit, limitHeader, NextentiConstants.DEFAULT_LIMIT);

        PayrollPageResponseDTO response = payrollService.list(userId, resolveOrganization(organizationId), workerId,
                employeeId, status, wageType, startDate, endDate, term, resolvedPage, resolvedLimit);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves one payroll's full details.
     *
     * @param id the payroll UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found or the caller is not authorized
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getPayroll(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getPayroll method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.getById(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a payroll while it is still in DRAFT status.
     *
     * @param id the payroll UUID
     * @param request the payroll update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll or worker is not found, the caller is not
     *                            authorized, the payroll is not in DRAFT status, or the period is
     *                            invalid or overlaps another payroll
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updatePayroll(@PathVariable UUID id,
                                                @RequestBody @Valid PayrollRequestDTO request,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updatePayroll method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Processes a DRAFT payroll, locking its already-calculated figures from further routine edits.
     *
     * @param id the payroll UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is not in DRAFT status
     */
    @PostMapping(path = "/{id}/process", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> processPayroll(@PathVariable UUID id,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside processPayroll method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.process(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Approves a PROCESSED payroll.
     *
     * @param id the payroll UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is not in PROCESSED status
     */
    @PostMapping(path = "/{id}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> approvePayroll(@PathVariable UUID id,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside approvePayroll method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.approve(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Marks an APPROVED payroll as paid, finalizing it.
     *
     * @param id the payroll UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is not in APPROVED status
     */
    @PostMapping(path = "/{id}/mark-paid", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> markPayrollPaid(@PathVariable UUID id,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside markPayrollPaid method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.markPaid(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Cancels a payroll that has not yet been paid.
     *
     * @param id the payroll UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link PayrollResponseDTO}
     * @throws SmartRoadException if the payroll is not found, the caller is not authorized, or
     *                            the payroll is already paid or already cancelled
     */
    @PostMapping(path = "/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> cancelPayroll(@PathVariable UUID id,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside cancelPayroll method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollResponseDTO response = payrollService.cancel(userId, id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a worker's full payroll history, most recent period first.
     *
     * @param workerId the worker UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link PayrollResponseDTO}
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @GetMapping(path = "/worker/{workerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getWorkerHistory(@PathVariable UUID workerId,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getWorkerHistory method--");

        UUID userId = RequestUtil.extractUserId();
        List<PayrollResponseDTO> response = payrollService.historyForWorker(userId, workerId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves aggregated payroll figures for the dashboard summary cards.
     *
     * @param organizationId the optional organization UUID; resolved from the caller when omitted
     * @param workerId the optional worker UUID filter
     * @param status the optional payroll status filter
     * @param wageType the optional wage type filter
     * @param startDate the optional period-overlap start filter
     * @param endDate the optional period-overlap end filter
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link PayrollSummaryResponseDTO}
     * @throws SmartRoadException if the caller is not authorized for the organization
     */
    @GetMapping(path = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getSummary(@RequestParam(required = false) UUID organizationId,
                                             @RequestParam(required = false) UUID workerId,
                                             @RequestParam(required = false) PayrollStatusEnum status,
                                             @RequestParam(required = false) WageTypeEnum wageType,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getSummary method--");

        UUID userId = RequestUtil.extractUserId();
        PayrollSummaryResponseDTO response = payrollService.summary(userId, resolveOrganization(organizationId),
                workerId, status, wageType, startDate, endDate);

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

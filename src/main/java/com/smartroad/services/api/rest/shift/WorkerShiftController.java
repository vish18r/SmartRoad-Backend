package com.smartroad.services.api.rest.shift;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.shift.ShiftAssignmentResponseDTO;
import com.smartroad.services.core.service.shift.ShiftAssignmentService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing a worker's shift from the worker's own point of view: their current
 * (or as-of-date) shift and their full assignment history. Kept separate from
 * {@link com.smartroad.services.api.rest.workers.WorkerController} so the worker CRUD/attendance
 * controller is never touched by this feature.
 * Handles HTTP requests and delegates all business logic to {@link ShiftAssignmentService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/workers")
public class WorkerShiftController {

    private static final Logger logger = LoggerFactory.getLogger(WorkerShiftController.class);

    private final ShiftAssignmentService shiftAssignmentService;

    /**
     * Constructs the controller with its required service dependency.
     *
     * @param shiftAssignmentService the worker shift assignment service
     */
    public WorkerShiftController(ShiftAssignmentService shiftAssignmentService) {
        this.shiftAssignmentService = shiftAssignmentService;
    }

    /**
     * Resolves the shift a worker is assigned to, as of a date. Defaults to today when no date
     * is supplied, giving the worker's currently active shift.
     *
     * @param workerId the worker UUID
     * @param date the optional date to resolve the shift as of; defaults to today
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ShiftAssignmentResponseDTO} in effect,
     *         or a null body when the worker holds no assignment covering that date
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @GetMapping(path = "/{workerId}/shift", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getCurrentShift(@PathVariable UUID workerId,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getCurrentShift method--");

        UUID userId = RequestUtil.extractUserId();
        ShiftAssignmentResponseDTO response = shiftAssignmentService.resolveForWorker(userId, workerId, date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a worker's full shift assignment history, most recently started first.
     *
     * @param workerId the worker UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ShiftAssignmentResponseDTO}
     * @throws SmartRoadException if the worker is not found or the caller is not authorized
     */
    @GetMapping(path = "/{workerId}/shift/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getShiftHistory(@PathVariable UUID workerId,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getShiftHistory method--");

        UUID userId = RequestUtil.extractUserId();
        List<ShiftAssignmentResponseDTO> response = shiftAssignmentService.historyForWorker(userId, workerId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

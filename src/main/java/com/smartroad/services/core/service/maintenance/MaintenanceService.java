package com.smartroad.services.core.service.maintenance;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.maintenance.MaintenanceCancelRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceCompleteRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceFilterDTO;
import com.smartroad.services.core.dto.maintenance.MaintenancePageResponseDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceRemindersDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceResponseDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceStartRequestDTO;
import com.smartroad.services.core.dto.maintenance.MaintenanceSummaryDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer contract for maintenance management: preventive/corrective maintenance record
 * CRUD, the start/complete/cancel workflow, derived DUE/OVERDUE status, dashboard summary, calendar
 * view, and on-demand reminder computation.
 *
 * @author Vishal
 * @version 1.0
 */
public interface MaintenanceService {

    /**
     * Creates a maintenance record (and, for a recurring preventive record, its next occurrence).
     *
     * @param userId the UUID of the user creating the record
     * @param organizationId the organization UUID
     * @param request the creation request
     * @return the created {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the machine/project/technician is not found or validation fails
     */
    MaintenanceResponseDTO create(UUID userId, UUID organizationId, MaintenanceRequestDTO request)
            throws SmartRoadException;

    /**
     * Lists a page of maintenance records matching the filters.
     *
     * @param filter the filter and pagination criteria
     * @return the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if a filter value or the date range is invalid
     */
    MaintenancePageResponseDTO list(MaintenanceFilterDTO filter) throws SmartRoadException;

    /**
     * Retrieves one maintenance record by ID.
     *
     * @param id the record UUID
     * @return the {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found
     */
    MaintenanceResponseDTO getById(UUID id) throws SmartRoadException;

    /**
     * Updates a maintenance record. Completed/cancelled records only accept notes (and, when
     * completed, actual cost and parts used); workflow status is never changed by this method.
     *
     * @param userId the UUID of the user performing the update
     * @param id the record UUID
     * @param request the update request
     * @return the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found, not editable, or validation fails
     */
    MaintenanceResponseDTO update(UUID userId, UUID id, MaintenanceRequestDTO request) throws SmartRoadException;

    /**
     * Deletes a maintenance record by cancelling it (soft cancel - the row is retained for history).
     *
     * @param userId the UUID of the user performing the delete
     * @param id the record UUID
     * @return the cancelled {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found, completed, or already cancelled
     */
    MaintenanceResponseDTO delete(UUID userId, UUID id) throws SmartRoadException;

    /**
     * Starts work on a SCHEDULED (including derived DUE/OVERDUE) record, moving it to IN_PROGRESS.
     *
     * @param userId the UUID of the user starting the work
     * @param id the record UUID
     * @param request the start request
     * @return the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found or not in a startable state
     */
    MaintenanceResponseDTO start(UUID userId, UUID id, MaintenanceStartRequestDTO request) throws SmartRoadException;

    /**
     * Completes an IN_PROGRESS record, recording the actual end, cost, and computed downtime.
     *
     * @param userId the UUID of the user completing the work
     * @param id the record UUID
     * @param request the completion request
     * @return the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found, not in progress, or the end precedes the start
     */
    MaintenanceResponseDTO complete(UUID userId, UUID id, MaintenanceCompleteRequestDTO request)
            throws SmartRoadException;

    /**
     * Cancels a SCHEDULED/DUE/OVERDUE/IN_PROGRESS record.
     *
     * @param userId the UUID of the user cancelling the record
     * @param id the record UUID
     * @param request the optional cancellation request (may be null)
     * @return the updated {@link MaintenanceResponseDTO}
     * @throws SmartRoadException if the record is not found or already completed/cancelled
     */
    MaintenanceResponseDTO cancel(UUID userId, UUID id, MaintenanceCancelRequestDTO request) throws SmartRoadException;

    /**
     * Lists a page of one machine's maintenance history, newest first.
     *
     * @param organizationId the organization UUID
     * @param machineId the machine UUID
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if the machine is not found
     */
    MaintenancePageResponseDTO getMachineHistory(UUID organizationId, UUID machineId, int page, int limit)
            throws SmartRoadException;

    /**
     * Lists a page of upcoming scheduled maintenance starting within the next {@code days} days.
     *
     * @param organizationId the organization UUID
     * @param days the look-ahead window in days, or null for the default
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link MaintenancePageResponseDTO}
     * @throws SmartRoadException if the look-ahead window is invalid
     */
    MaintenancePageResponseDTO getUpcoming(UUID organizationId, Integer days, int page, int limit)
            throws SmartRoadException;

    /**
     * Lists a page of overdue maintenance (scheduled work whose deadline has passed), oldest deadline first.
     *
     * @param organizationId the organization UUID
     * @param page the one-based page number
     * @param limit the page size
     * @return the {@link MaintenancePageResponseDTO}
     */
    MaintenancePageResponseDTO getOverdue(UUID organizationId, int page, int limit);

    /**
     * Computes the maintenance dashboard summary.
     *
     * @param organizationId the organization UUID
     * @param startDate the inclusive period start, or null for the first day of the current month
     * @param endDate the inclusive period end, or null for the last day of the current month
     * @return the {@link MaintenanceSummaryDTO}
     * @throws SmartRoadException if the date range is invalid
     */
    MaintenanceSummaryDTO getSummary(UUID organizationId, LocalDate startDate, LocalDate endDate)
            throws SmartRoadException;

    /**
     * Lists every maintenance record scheduled to start in a date range, for the calendar view.
     *
     * @param organizationId the organization UUID
     * @param startDate the inclusive range start, or null for the first day of the current month
     * @param endDate the inclusive range end, or null for the last day of the current month
     * @return list of {@link MaintenanceResponseDTO} ordered by scheduled start
     * @throws SmartRoadException if the range is invalid or too large
     */
    List<MaintenanceResponseDTO> getCalendar(UUID organizationId, LocalDate startDate, LocalDate endDate)
            throws SmartRoadException;

    /**
     * Computes which due and overdue reminders would need to be sent right now. Nothing is
     * dispatched; there is no scheduler in this codebase to send them periodically.
     *
     * @param organizationId the organization UUID
     * @return the {@link MaintenanceRemindersDTO}
     */
    MaintenanceRemindersDTO getReminders(UUID organizationId);
}

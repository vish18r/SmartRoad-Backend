package com.smartroad.services.core.service.tracking;

import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.tracking.AttendanceDTO.DailySummary;
import com.smartroad.services.core.dto.tracking.AttendanceDTO.EventResponse;
import com.smartroad.services.core.dto.tracking.AttendanceDTO.Position;
import com.smartroad.services.core.dto.tracking.AttendanceDTO.Session;
import com.smartroad.services.core.service.workers.WorkerService;
import com.smartroad.services.domain.entity.tracking.WorkerCheckinEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import com.smartroad.services.domain.repository.tracking.WorkerCheckinRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for worker attendance: check-in, check-out and the daily summaries.
 * Each row of the check-in table is one session; the daily summary derives the first check-in,
 * the last check-out and the total work time from all sessions of the day. All timestamps come
 * from the server clock, and identity is always resolved on the server from the QR code, the
 * face or an authorized worker id.
 */
@Service
public class AttendanceService {

    private static final String CHECKED_IN = "CHECKED_IN";
    private static final String CHECKED_OUT = "CHECKED_OUT";
    private static final String NOT_CHECKED_IN = "NOT_CHECKED_IN";
    private static final String MANUAL = "MANUAL";

    private final TrackingService trackingService;
    private final WorkerService workerService;
    private final WorkerCheckinRepository checkinRepository;

    /**
     * Constructs the service with required dependencies.
     *
     * @param trackingService the service that owns the check-in and check-out session rules
     * @param workerService the worker service used to identify and authorize workers
     * @param checkinRepository the check-in session repository
     */
    public AttendanceService(TrackingService trackingService, WorkerService workerService,
                             WorkerCheckinRepository checkinRepository) {
        this.trackingService = trackingService;
        this.workerService = workerService;
        this.checkinRepository = checkinRepository;
    }

    /**
     * Checks a worker in. Rejected when the worker already has an open session.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param workerId the worker to check in, or null to act on the caller's own id
     * @param method the check-in method supplied by the client
     * @param position the optional GPS position
     * @return the {@link EventResponse} for the new session
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized or already checked in
     */
    @Transactional
    public EventResponse checkIn(UUID userId, UUID projectId, UUID workerId, String method, Position position) throws SmartRoadException {
        WorkerEntity worker = resolveWorker(userId, projectId, workerId);
        return recordCheckIn(projectId, worker, worker.getId(), normalizeMethod(method), position);
    }

    /**
     * Checks a worker out. Rejected when the worker has no open session.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param workerId the worker to check out, or null to act on the caller's own id
     * @param method the check-out method supplied by the client
     * @param position the optional GPS position
     * @return the {@link EventResponse} for the closed session
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized or not checked in
     */
    @Transactional
    public EventResponse checkOut(UUID userId, UUID projectId, UUID workerId, String method, Position position) throws SmartRoadException {
        WorkerEntity worker = resolveWorker(userId, projectId, workerId);
        return recordCheckOut(projectId, worker, worker.getId(), normalizeMethod(method), position);
    }

    /**
     * Identifies a worker from a captured face and checks them in, or out when a session is open.
     * The face is only compared, never registered or changed.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param faceDescriptor the captured face descriptor as a JSON array
     * @param position the optional GPS position
     * @return the {@link EventResponse} for the check-in or check-out
     * @throws SmartRoadException if the face is not recognized or the worker is inactive
     */
    @Transactional
    public EventResponse byFace(UUID userId, UUID projectId, String faceDescriptor, Position position) throws SmartRoadException {
        WorkerEntity worker = workerService.matchFace(userId, projectId, faceDescriptor);
        return toggle(projectId, worker, "FACE", position);
    }

    /**
     * Identifies a worker from a scanned worker QR code and checks them in, or out when a session is open.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param qrCode the scanned QR text, e.g. WORKER:SR1
     * @param position the optional GPS position
     * @return the {@link EventResponse} for the check-in or check-out
     * @throws SmartRoadException if the QR code is invalid, the worker is unknown or inactive
     */
    @Transactional
    public EventResponse byQr(UUID userId, UUID projectId, String qrCode, Position position) throws SmartRoadException {
        WorkerEntity worker = workerService.identifyByQr(userId, projectId, qrCode);
        return toggle(projectId, worker, "QR", position);
    }

    /**
     * Returns the daily attendance of every worker who checked in on a project on a date.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param date the date to report on
     * @return one {@link DailySummary} per worker, ordered by employee ID
     * @throws SmartRoadException if the project is unknown or the user is not authorized
     */
    @Transactional(readOnly = true)
    public List<DailySummary> dailyForProject(UUID userId, UUID projectId, LocalDate date) throws SmartRoadException {
        workerService.requireProjectAccess(userId, projectId);

        Map<UUID, List<WorkerCheckinEntity>> byWorker = checkinRepository.findByProjectAndDate(projectId, date).stream()
                .collect(Collectors.groupingBy(WorkerCheckinEntity::getWorkerId, LinkedHashMap::new, Collectors.toList()));
        Map<UUID, WorkerEntity> workers = workerService.findAllById(byWorker.keySet());

        return byWorker.entrySet().stream()
                .map(entry -> buildDaily(entry.getKey(), workers.get(entry.getKey()), date, entry.getValue()))
                .sorted(Comparator.comparing(DailySummary::employeeId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * Returns one worker's attendance for a date, across every project.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @param date the date to report on
     * @return the {@link DailySummary}; its status is NOT_CHECKED_IN when the worker has no session that day
     * @throws SmartRoadException if the worker is unknown or the user is not authorized
     */
    @Transactional(readOnly = true)
    public DailySummary dailyForWorker(UUID userId, UUID workerId, LocalDate date) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, workerId);
        return buildDaily(workerId, worker, date, sessionsBetween(workerId, date, date));
    }

    /**
     * Returns one worker's daily attendance for every date in a range that has a session.
     *
     * @param userId the UUID of the requesting user
     * @param workerId the worker UUID
     * @param from the first date, inclusive
     * @param to the last date, inclusive
     * @return one {@link DailySummary} per date with at least one session, oldest first
     * @throws SmartRoadException if the worker is unknown or the user is not authorized
     */
    @Transactional(readOnly = true)
    public List<DailySummary> summaryForWorker(UUID userId, UUID workerId, LocalDate from, LocalDate to) throws SmartRoadException {
        WorkerEntity worker = workerService.requireWorkerAccess(userId, workerId);

        return sessionsBetween(workerId, from, to).stream()
                .collect(Collectors.groupingBy(s -> s.getCheckInTime().toLocalDate(), TreeMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> buildDaily(workerId, worker, entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Resolves the worker an explicit check-in or check-out acts on.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project UUID
     * @param workerId the client-supplied worker UUID
     * @return the authorized active worker
     * @throws SmartRoadException if no worker was given, or the worker is unknown, inactive or unauthorized
     */
    private WorkerEntity resolveWorker(UUID userId, UUID projectId, UUID workerId) throws SmartRoadException {
        if (workerId == null) {
            throw new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.SERVICE_INVALID_INPUT,
                "worker.id.required"
            );
        }
        return workerService.requireActiveWorkerForProject(userId, workerId, projectId);
    }

    /**
     * Checks the worker out when a session is open, otherwise in.
     *
     * @param projectId the project UUID
     * @param worker the identified worker
     * @param method QR or FACE
     * @param position the optional GPS position
     * @return the {@link EventResponse} for whichever happened
     * @throws SmartRoadException if the session rules reject the action
     */
    private EventResponse toggle(UUID projectId, WorkerEntity worker, String method, Position position) throws SmartRoadException {
        return trackingService.hasActiveCheckin(projectId, worker.getId())
                ? recordCheckOut(projectId, worker, worker.getId(), method, position)
                : recordCheckIn(projectId, worker, worker.getId(), method, position);
    }

    /**
     * Opens a session through the tracking service and builds the response.
     *
     * @param projectId the project UUID
     * @param worker the worker, or null when acting on a bare user id
     * @param workerId the id the session is recorded against
     * @param method the check-in method
     * @param position the optional GPS position
     * @return the {@link EventResponse} with status CHECKED_IN
     * @throws SmartRoadException if the worker already has an open session
     */
    private EventResponse recordCheckIn(UUID projectId, WorkerEntity worker, UUID workerId, String method, Position position) throws SmartRoadException {
        WorkerCheckinEntity session = trackingService.workerCheckIn(projectId, workerId,
                latitude(position), longitude(position), accuracy(position), method);
        return event(worker, session, CHECKED_IN);
    }

    /**
     * Closes the open session through the tracking service and builds the response.
     *
     * @param projectId the project UUID
     * @param worker the worker, or null when acting on a bare user id
     * @param workerId the id the session is recorded against
     * @param method the check-out method
     * @param position the optional GPS position
     * @return the {@link EventResponse} with status CHECKED_OUT
     * @throws SmartRoadException if the worker has no open session
     */
    private EventResponse recordCheckOut(UUID projectId, WorkerEntity worker, UUID workerId, String method, Position position) throws SmartRoadException {
        WorkerCheckinEntity session = trackingService.workerCheckOut(projectId, workerId,
                latitude(position), longitude(position), accuracy(position), method);
        return event(worker, session, CHECKED_OUT);
    }

    /**
     * Builds the check-in or check-out response for a session.
     *
     * @param worker the worker, or null when unknown
     * @param session the session
     * @param status CHECKED_IN or CHECKED_OUT
     * @return the {@link EventResponse}
     */
    private EventResponse event(WorkerEntity worker, WorkerCheckinEntity session, String status) {
        boolean closed = CHECKED_OUT.equals(status);
        return new EventResponse(session.getWorkerId(), worker == null ? null : worker.getEmployeeId(), displayName(worker),
                session.getCheckInTime().toLocalDate(), session.getCheckInTime(),
                closed ? session.getCheckOutTime() : null, session.getCheckInMethod(),
                closed ? session.getCheckOutMethod() : null,
                closed ? formatDuration(secondsBetween(session)) : null, status);
    }

    /**
     * Loads a worker's sessions that started within a date range.
     *
     * @param workerId the worker UUID
     * @param from the first date, inclusive
     * @param to the last date, inclusive
     * @return the sessions, earliest first
     */
    private List<WorkerCheckinEntity> sessionsBetween(UUID workerId, LocalDate from, LocalDate to) {
        ZoneId zone = ZoneId.systemDefault();
        return checkinRepository.findByWorkerAndPeriod(workerId,
                from.atStartOfDay(zone).toOffsetDateTime(), to.plusDays(1).atStartOfDay(zone).toOffsetDateTime());
    }

    /**
     * Builds a worker's daily summary from that day's sessions.
     *
     * @param workerId the worker UUID
     * @param worker the worker, or null when unknown
     * @param date the date
     * @param sessions the worker's sessions that day, in any order
     * @return the {@link DailySummary}
     */
    private DailySummary buildDaily(UUID workerId, WorkerEntity worker, LocalDate date, List<WorkerCheckinEntity> sessions) {
        List<WorkerCheckinEntity> ordered = sessions.stream()
                .sorted(Comparator.comparing(WorkerCheckinEntity::getCheckInTime)).toList();

        OffsetDateTime first = ordered.isEmpty() ? null : ordered.get(0).getCheckInTime();
        OffsetDateTime last = ordered.stream().map(WorkerCheckinEntity::getCheckOutTime)
                .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
        long total = ordered.stream().filter(s -> s.getCheckOutTime() != null).mapToLong(this::secondsBetween).sum();
        boolean open = ordered.stream().anyMatch(s -> s.getCheckOutTime() == null);
        String status = ordered.isEmpty() ? NOT_CHECKED_IN : open ? CHECKED_IN : CHECKED_OUT;

        List<Session> list = ordered.stream().map(s -> new Session(s.getId(), s.getProjectId(), s.getCheckInTime(),
                s.getCheckOutTime(), s.getCheckInMethod(), s.getCheckOutMethod(),
                s.getCheckOutTime() == null ? null : secondsBetween(s))).toList();

        return new DailySummary(workerId, worker == null ? null : worker.getEmployeeId(), displayName(worker), date,
                first, last, total, formatDuration(total), status, list);
    }

    /**
     * Length of a closed session in whole seconds, never negative.
     *
     * @param session a session with both times set
     * @return the seconds between check-in and check-out
     */
    private long secondsBetween(WorkerCheckinEntity session) {
        return Math.max(0, Duration.between(session.getCheckInTime(), session.getCheckOutTime()).getSeconds());
    }

    /**
     * Formats seconds as HH:mm:ss.
     *
     * @param totalSeconds the duration in seconds
     * @return the formatted duration
     */
    private String formatDuration(long totalSeconds) {
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }

    /**
     * A worker's display name.
     *
     * @param worker the worker, or null
     * @return first and last name, or null when the worker is unknown
     */
    private String displayName(WorkerEntity worker) {
        return worker == null ? null
                : (worker.getFirstName() + " " + (worker.getLastName() == null ? "" : worker.getLastName())).trim();
    }

    /**
     * Restricts a client-supplied method to the supported values.
     *
     * @param method the method supplied by the client, possibly null
     * @return QR, FACE or MANUAL
     */
    private String normalizeMethod(String method) {
        String upper = method == null ? MANUAL : method.trim().toUpperCase();
        return "QR".equals(upper) || "FACE".equals(upper) ? upper : MANUAL;
    }

    /**
     * Reads the latitude from an optional position.
     *
     * @param position the position, possibly null
     * @return the latitude, or null when no position was supplied
     */
    private BigDecimal latitude(Position position) {
        return position == null ? null : position.latitude();
    }

    /**
     * Reads the longitude from an optional position.
     *
     * @param position the position, possibly null
     * @return the longitude, or null when no position was supplied
     */
    private BigDecimal longitude(Position position) {
        return position == null ? null : position.longitude();
    }

    /**
     * Reads the accuracy from an optional position.
     *
     * @param position the position, possibly null
     * @return the accuracy, or null when no position was supplied
     */
    private BigDecimal accuracy(Position position) {
        return position == null ? null : position.accuracy();
    }
}

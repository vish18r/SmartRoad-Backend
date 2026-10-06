package com.smartroad.services.api.rest.tracking;

import java.util.List;
import java.time.OffsetDateTime;
import com.smartroad.services.core.dto.tracking.AttendanceDTO;
import com.smartroad.services.core.service.tracking.AttendanceService;
import com.smartroad.services.core.service.workers.WorkerService;
import org.springframework.format.annotation.DateTimeFormat;
import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.service.tracking.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST controller for construction site tracking features.
 * Handles GPS tracking, worker check-in/check-out, QR scanning, and work photos.
 */
@RestController
@RequestMapping("/api/v1/nextenti/tracking")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class TrackingController {

    private final TrackingService trackingService;
    private final WorkerService workerService;
    private final AttendanceService attendanceService;

    /**
     * Records GPS location for a worker.
     */
    @PostMapping(path = "/gps/record", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> recordGPSLocation(
            @RequestBody RecordGPSRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside recordGPSLocation method--");

        UUID userId = RequestUtil.extractUserId();
        trackingService.recordGPSLocation(
                request.projectId,
                userId,
                request.latitude,
                request.longitude,
                request.accuracy
        );

        return ResponseEntity.status(HttpStatus.OK).body("GPS location recorded");
    }

    /**
     * Worker check-in. The time is taken from the server clock. Rejected when the worker already has an open session.
     *
     * @param request the project, the optional worker, the method and the optional GPS position
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link AttendanceDTO.EventResponse}
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized or already checked in
     */
    @PostMapping(path = "/checkin", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> workerCheckIn(
            @RequestBody CheckinCheckoutRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside workerCheckIn method--");

        UUID userId = RequestUtil.extractUserId();
        AttendanceDTO.EventResponse response = attendanceService.checkIn(userId, request.projectId, request.workerId,
                request.method, new AttendanceDTO.Position(request.latitude, request.longitude, request.accuracy));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Worker check-out. The time is taken from the server clock. Rejected when the worker has no open session.
     *
     * @param request the project, the optional worker, the method and the optional GPS position
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link AttendanceDTO.EventResponse}
     * @throws SmartRoadException if the worker is unknown, inactive, unauthorized or not checked in
     */
    @PostMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> workerCheckOut(
            @RequestBody CheckinCheckoutRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside workerCheckOut method--");

        UUID userId = RequestUtil.extractUserId();
        AttendanceDTO.EventResponse response = attendanceService.checkOut(userId, request.projectId, request.workerId,
                request.method, new AttendanceDTO.Position(request.latitude, request.longitude, request.accuracy));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Face attendance. Identifies the worker from a captured face against the registered faces, then checks
     * the worker in, or out when a session is open. The face is never registered or changed here, and an
     * unrecognized face creates nothing.
     *
     * @param request the project, the captured face descriptor and the optional GPS position
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link AttendanceDTO.EventResponse}
     * @throws SmartRoadException if the face is not recognized, the worker is inactive, or the caller is not authorized
     */
    @PostMapping(path = "/checkin/face", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> faceCheckIn(
            @RequestBody FaceCheckinRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside faceCheckIn method--");

        UUID userId = RequestUtil.extractUserId();
        AttendanceDTO.EventResponse response = attendanceService.byFace(userId, request.projectId, request.faceDescriptor,
                new AttendanceDTO.Position(request.latitude, request.longitude, request.accuracy));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * QR attendance. Identifies the worker from a scanned worker QR code, then checks the worker in, or out
     * when a session is open.
     *
     * @param request the project, the scanned QR text and the optional GPS position
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link AttendanceDTO.EventResponse}
     * @throws SmartRoadException if the QR code is invalid, the worker is unknown or inactive, or the caller is not authorized
     */
    @PostMapping(path = "/checkin/qr", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> qrCheckIn(
            @RequestBody QrCheckinRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside qrCheckIn method--");

        UUID userId = RequestUtil.extractUserId();
        AttendanceDTO.EventResponse response = attendanceService.byQr(userId, request.projectId, request.qrCode,
                new AttendanceDTO.Position(request.latitude, request.longitude, request.accuracy));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Scan QR code.
     */
    @PostMapping(path = "/qr/scan", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> scanQRCode(
            @RequestBody ScanQRCodeRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside scanQRCode method--");

        UUID userId = RequestUtil.extractUserId();
        trackingService.scanQRCode(
                request.projectId,
                request.qrCode,
                userId,
                request.latitude,
                request.longitude
        );

        return ResponseEntity.status(HttpStatus.OK).body("QR code scanned");
    }

    /**
     * Upload work photo.
     */
    @PostMapping(path = "/photo/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> uploadWorkPhoto(
            @RequestBody UploadWorkPhotoRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside uploadWorkPhoto method--");

        UUID userId = RequestUtil.extractUserId();
        trackingService.recordWorkPhoto(
                request.projectId,
                request.photoType,
                request.photoUrl,
                request.latitude,
                request.longitude,
                request.description,
                request.workArea,
                userId
        );

        return ResponseEntity.status(HttpStatus.OK).body("Photo uploaded");
    }

    /**
     * Get checkins by date.
     */
    @GetMapping(path = "/checkins", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCheckinsByDate(
            @RequestParam UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside getCheckinsByDate method--");

        LocalDate queryDate = date != null ? date : LocalDate.now();
        var checkins = trackingService.getCheckinsByDate(projectId, queryDate);
        var workers = workerService.findAllById(checkins.stream().map(c -> c.getWorkerId()).distinct().toList());

        List<CheckinView> response = checkins.stream().map(c -> {
            var worker = workers.get(c.getWorkerId());
            String name = worker == null ? null
                    : (worker.getFirstName() + " " + (worker.getLastName() == null ? "" : worker.getLastName())).trim();
            return new CheckinView(c.getId(), c.getWorkerId(), name, worker == null ? null : worker.getEmployeeId(),
                    c.getCheckInTime(), c.getCheckOutTime(), c.getDurationMinutes(), c.getStatus(), c.getCheckInMethod());
        }).toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Request body for face check-in.
     */
    public static class FaceCheckinRequest {
        public UUID projectId;
        public String faceDescriptor;
        public BigDecimal latitude;
        public BigDecimal longitude;
        public BigDecimal accuracy;
    }

    /**
     * Request body for QR attendance.
     */
    public static class QrCheckinRequest {
        public UUID projectId;
        public String qrCode;
        public BigDecimal latitude;
        public BigDecimal longitude;
        public BigDecimal accuracy;
    }

    /**
     * One check-in row as returned to the web client, enriched with the worker's name and employee ID.
     *
     * @param id the check-in UUID
     * @param workerId the worker UUID
     * @param workerName the worker's display name
     * @param employeeId the worker's employee ID
     * @param checkInTime the check-in time
     * @param checkOutTime the check-out time, if any
     * @param durationMinutes the minutes between check-in and check-out
     * @param status the check-in status
     * @param checkInMethod how the worker checked in: QR, FACE or MANUAL
     */
    public record CheckinView(UUID id, UUID workerId, String workerName, String employeeId,
                              OffsetDateTime checkInTime, OffsetDateTime checkOutTime,
                              Integer durationMinutes, String status, String checkInMethod) {}

    /**
     * Daily attendance of every worker who checked in on a project on a date: first check-in, last
     * check-out, total work time (the sum of the sessions) and the sessions themselves.
     *
     * @param projectId the project UUID
     * @param date the date to report on, today when omitted
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link AttendanceDTO.DailySummary}
     * @throws SmartRoadException if the project is unknown or the caller is not authorized
     */
    @GetMapping(path = "/attendance/daily", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getDailyAttendance(
            @RequestParam UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside getDailyAttendance method--");

        UUID userId = RequestUtil.extractUserId();
        List<AttendanceDTO.DailySummary> response = attendanceService.dailyForProject(userId, projectId, date != null ? date : LocalDate.now());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * One worker's attendance for a date across every project.
     *
     * @param workerId the worker UUID
     * @param date the date to report on, today when omitted
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link AttendanceDTO.DailySummary}
     * @throws SmartRoadException if the worker is unknown or the caller is not authorized
     */
    @GetMapping(path = "/attendance/daily/{workerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getWorkerDailyAttendance(
            @PathVariable UUID workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside getWorkerDailyAttendance method--");

        UUID userId = RequestUtil.extractUserId();
        AttendanceDTO.DailySummary response = attendanceService.dailyForWorker(userId, workerId, date != null ? date : LocalDate.now());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * One worker's daily attendance for every date in a range that has a session. The range defaults to the
     * last 30 days.
     *
     * @param workerId the worker UUID
     * @param from the first date, inclusive
     * @param to the last date, inclusive
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link AttendanceDTO.DailySummary}
     * @throws SmartRoadException if the worker is unknown or the caller is not authorized
     */
    @GetMapping(path = "/attendance/summary/{workerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getWorkerAttendanceSummary(
            @PathVariable UUID workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside getWorkerAttendanceSummary method--");

        UUID userId = RequestUtil.extractUserId();
        LocalDate end = to != null ? to : LocalDate.now();
        List<AttendanceDTO.DailySummary> response = attendanceService.summaryForWorker(userId, workerId,
                from != null ? from : end.minusDays(29), end);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get work photos by date.
     */
    @GetMapping(path = "/photos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getWorkPhotosByDate(
            @RequestParam UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside getWorkPhotosByDate method--");

        LocalDate queryDate = date != null ? date : LocalDate.now();
        var photos = trackingService.getWorkPhotosByDate(projectId, queryDate);

        return ResponseEntity.status(HttpStatus.OK).body(photos);
    }

    // Request/Response DTOs
    public static class RecordGPSRequest {
        public UUID projectId;
        public BigDecimal latitude;
        public BigDecimal longitude;
        public BigDecimal accuracy;
    }

    public static class CheckinCheckoutRequest {
        public UUID projectId;
        public UUID workerId;
        public String method;
        public BigDecimal latitude;
        public BigDecimal longitude;
        public BigDecimal accuracy;
    }

    public static class ScanQRCodeRequest {
        public UUID projectId;
        public String qrCode;
        public BigDecimal latitude;
        public BigDecimal longitude;
    }

    public static class UploadWorkPhotoRequest {
        public UUID projectId;
        public String photoType;
        public String photoUrl;
        public BigDecimal latitude;
        public BigDecimal longitude;
        public String description;
        public String workArea;
    }
}

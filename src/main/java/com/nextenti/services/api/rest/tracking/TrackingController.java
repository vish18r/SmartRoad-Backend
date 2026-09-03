package com.nextenti.services.api.rest.tracking;

import com.nextenti.services.api.utils.RequestUtil;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.service.tracking.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/nextenti/tracking")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER')")
public class TrackingController {

    private final TrackingService trackingService;

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
     * Worker check-in with GPS location.
     */
    @PostMapping(path = "/checkin", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> workerCheckIn(
            @RequestBody CheckinCheckoutRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside workerCheckIn method--");

        UUID userId = RequestUtil.extractUserId();
        trackingService.workerCheckIn(
                request.projectId,
                userId,
                request.latitude,
                request.longitude,
                request.accuracy
        );

        return ResponseEntity.status(HttpStatus.OK).body("Check-in successful");
    }

    /**
     * Worker check-out with GPS location.
     */
    @PostMapping(path = "/checkout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> workerCheckOut(
            @RequestBody CheckinCheckoutRequest request,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside workerCheckOut method--");

        UUID userId = RequestUtil.extractUserId();
        trackingService.workerCheckOut(
                request.projectId,
                userId,
                request.latitude,
                request.longitude,
                request.accuracy
        );

        return ResponseEntity.status(HttpStatus.OK).body("Check-out successful");
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
            @RequestParam(required = false) LocalDate date,
            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        log.info("--Inside getCheckinsByDate method--");

        LocalDate queryDate = date != null ? date : LocalDate.now();
        var checkins = trackingService.getCheckinsByDate(projectId, queryDate);

        return ResponseEntity.status(HttpStatus.OK).body(checkins);
    }

    /**
     * Get work photos by date.
     */
    @GetMapping(path = "/photos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getWorkPhotosByDate(
            @RequestParam UUID projectId,
            @RequestParam(required = false) LocalDate date,
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

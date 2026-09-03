package com.nextenti.services.core.service.tracking;

import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.domain.entity.tracking.GPSLocationEntity;
import com.nextenti.services.domain.entity.tracking.WorkerCheckinEntity;
import com.nextenti.services.domain.entity.tracking.QRCodeEntity;
import com.nextenti.services.domain.entity.tracking.QRScanEntity;
import com.nextenti.services.domain.entity.tracking.WorkPhotoEntity;
import com.nextenti.services.domain.repository.tracking.GPSLocationRepository;
import com.nextenti.services.domain.repository.tracking.WorkerCheckinRepository;
import com.nextenti.services.domain.repository.tracking.QRCodeRepository;
import com.nextenti.services.domain.repository.tracking.QRScanRepository;
import com.nextenti.services.domain.repository.tracking.WorkPhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for construction site tracking features.
 * Handles GPS tracking, worker check-in/check-out, QR code scanning, and work photos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final GPSLocationRepository gpsLocationRepository;
    private final WorkerCheckinRepository workerCheckinRepository;
    private final QRCodeRepository qrCodeRepository;
    private final QRScanRepository qrScanRepository;
    private final WorkPhotoRepository workPhotoRepository;

    /**
     * Records a GPS location for a worker.
     */
    @Transactional
    public void recordGPSLocation(UUID projectId, UUID workerId, BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy) {
        GPSLocationEntity location = new GPSLocationEntity();
        location.setProjectId(projectId);
        location.setWorkerId(workerId);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);
        location.setTimestamp(OffsetDateTime.now());

        gpsLocationRepository.save(location);
        log.info("GPS location recorded for worker {} at project {}", workerId, projectId);
    }

    /**
     * Performs worker check-in with GPS location.
     */
    @Transactional
    public void workerCheckIn(UUID projectId, UUID workerId, BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy) throws SmartRoadException {
        LocalDate today = LocalDate.now();
        var existing = workerCheckinRepository.findTodayActiveCheckin(projectId, workerId, today);

        if (existing.isPresent()) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "worker.already.checked.in");
        }

        WorkerCheckinEntity checkin = new WorkerCheckinEntity();
        checkin.setProjectId(projectId);
        checkin.setWorkerId(workerId);
        checkin.setCheckInTime(OffsetDateTime.now());
        checkin.setCheckInLatitude(latitude);
        checkin.setCheckInLongitude(longitude);
        checkin.setCheckInAccuracy(accuracy);
        checkin.setStatus("ACTIVE");

        workerCheckinRepository.save(checkin);
        log.info("Worker {} checked in at project {}", workerId, projectId);
    }

    /**
     * Performs worker check-out with GPS location.
     */
    @Transactional
    public void workerCheckOut(UUID projectId, UUID workerId, BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy) throws SmartRoadException {
        LocalDate today = LocalDate.now();
        var checkin = workerCheckinRepository.findTodayActiveCheckin(projectId, workerId, today)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "checkin.not.found"));

        checkin.setCheckOutTime(OffsetDateTime.now());
        checkin.setCheckOutLatitude(latitude);
        checkin.setCheckOutLongitude(longitude);
        checkin.setCheckOutAccuracy(accuracy);
        checkin.setStatus("COMPLETED");

        long durationMinutes = ChronoUnit.MINUTES.between(checkin.getCheckInTime(), checkin.getCheckOutTime());
        checkin.setDurationMinutes((int) durationMinutes);

        workerCheckinRepository.save(checkin);
        log.info("Worker {} checked out from project {} - Duration: {} minutes", workerId, projectId, durationMinutes);
    }

    /**
     * Scans a QR code.
     */
    @Transactional
    public void scanQRCode(UUID projectId, String qrCode, UUID scannedBy, BigDecimal latitude, BigDecimal longitude) throws SmartRoadException {
        QRCodeEntity qrEntity = qrCodeRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "qr.code.not.found"));

        QRScanEntity scan = new QRScanEntity();
        scan.setQrCodeId(qrEntity.getId());
        scan.setProjectId(projectId);
        scan.setScannedBy(scannedBy);
        scan.setScanLatitude(latitude);
        scan.setScanLongitude(longitude);
        scan.setScanTime(OffsetDateTime.now());
        scan.setStatus("SCANNED");

        qrScanRepository.save(scan);
        log.info("QR code {} scanned at project {}", qrCode, projectId);
    }

    /**
     * Records a work photo.
     */
    @Transactional
    public void recordWorkPhoto(UUID projectId, String photoType, String photoUrl, BigDecimal latitude, BigDecimal longitude, String description, String workArea, UUID uploadedBy) {
        WorkPhotoEntity photo = new WorkPhotoEntity();
        photo.setProjectId(projectId);
        photo.setPhotoType(photoType);
        photo.setPhotoUrl(photoUrl);
        photo.setPhotoDate(OffsetDateTime.now());
        photo.setLatitude(latitude);
        photo.setLongitude(longitude);
        photo.setDescription(description);
        photo.setWorkArea(workArea);
        photo.setUploadedBy(uploadedBy);

        workPhotoRepository.save(photo);
        log.info("Work photo recorded for project {}", projectId);
    }

    /**
     * Retrieves recent worker check-ins for a project.
     */
    public List<WorkerCheckinEntity> getCheckinsByDate(UUID projectId, LocalDate date) {
        return workerCheckinRepository.findByProjectAndDate(projectId, date);
    }

    /**
     * Retrieves work photos for a specific date.
     */
    public List<WorkPhotoEntity> getWorkPhotosByDate(UUID projectId, LocalDate date) {
        return workPhotoRepository.findByProjectAndDate(projectId, date);
    }
}

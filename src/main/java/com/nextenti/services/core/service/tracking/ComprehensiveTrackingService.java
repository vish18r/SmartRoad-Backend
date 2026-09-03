package com.nextenti.services.core.service.tracking;

import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.tracking.TrackingDTO;
import com.nextenti.services.core.dto.tracking.QCAndSafetyDTOs;
import com.nextenti.services.domain.entity.tracking.*;
import com.nextenti.services.domain.repository.tracking.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Comprehensive tracking service for all construction site operations.
 * Handles GPS tracking, QC, safety, reports, and project evidence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveTrackingService {

    private final GPSLocationRepository gpsLocationRepository;
    private final WorkerCheckinRepository workerCheckinRepository;
    private final QRCodeRepository qrCodeRepository;
    private final QRScanRepository qrScanRepository;
    private final WorkPhotoRepository workPhotoRepository;
    private final DailyPhotoTimelineRepository dailyPhotoTimelineRepository;
    private final WorkCompletionCertificateRepository certificateRepository;

    // ==================== GPS & ATTENDANCE ====================

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
        log.info("Worker {} checked out - Duration: {} minutes", workerId, durationMinutes);
    }

    // ==================== QR CODE & ASSET TRACKING ====================

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

    // ==================== WORK PHOTOS & EVIDENCE ====================

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

    @Transactional
    public void recordDailyPhotoTimeline(UUID projectId, LocalDate date, String morningUrl, String afternoonUrl, String eveningUrl, String summary, String weather) {
        var existing = dailyPhotoTimelineRepository.findByProjectIdAndTimelineDate(projectId, date);
        DailyPhotoTimelineEntity timeline = existing.orElseGet(DailyPhotoTimelineEntity::new);
        timeline.setProjectId(projectId);
        timeline.setTimelineDate(date);
        if (morningUrl != null) timeline.setMorningPhotoUrl(morningUrl);
        if (afternoonUrl != null) timeline.setAfternoonPhotoUrl(afternoonUrl);
        if (eveningUrl != null) timeline.setEveningPhotoUrl(eveningUrl);
        if (summary != null) timeline.setSummary(summary);
        if (weather != null) timeline.setWeatherCondition(weather);
        dailyPhotoTimelineRepository.save(timeline);
        log.info("Daily photo timeline updated for project {} on {}", projectId, date);
    }

    @Transactional
    public void recordWorkCompletion(UUID projectId, String workSection, String photoUrl, String signatureUrl, String remarks, UUID completedBy) {
        WorkCompletionCertificateEntity cert = new WorkCompletionCertificateEntity();
        cert.setProjectId(projectId);
        cert.setWorkSection(workSection);
        cert.setCompletionDate(OffsetDateTime.now());
        cert.setCompletedBy(completedBy);
        cert.setPhotoUrl(photoUrl);
        cert.setSignatureUrl(signatureUrl);
        cert.setStatus("DRAFT");
        cert.setRemarks(remarks);
        certificateRepository.save(cert);
        log.info("Work completion recorded for project {} - section: {}", projectId, workSection);
    }

    // ==================== RETRIEVAL METHODS ====================

    public List<TrackingDTO.WorkerCheckinResponse> getCheckinsByDate(UUID projectId, LocalDate date) {
        return workerCheckinRepository.findByProjectAndDate(projectId, date).stream()
                .map(e -> TrackingDTO.WorkerCheckinResponse.builder()
                        .id(e.getId())
                        .projectId(e.getProjectId())
                        .workerId(e.getWorkerId())
                        .checkInTime(e.getCheckInTime())
                        .checkOutTime(e.getCheckOutTime())
                        .status(e.getStatus())
                        .durationMinutes(e.getDurationMinutes())
                        .build())
                .collect(Collectors.toList());
    }

    public List<TrackingDTO.WorkPhotoResponse> getWorkPhotosByDate(UUID projectId, LocalDate date) {
        return workPhotoRepository.findByProjectAndDate(projectId, date).stream()
                .map(e -> TrackingDTO.WorkPhotoResponse.builder()
                        .id(e.getId())
                        .projectId(e.getProjectId())
                        .photoType(e.getPhotoType())
                        .photoUrl(e.getPhotoUrl())
                        .description(e.getDescription())
                        .workArea(e.getWorkArea())
                        .build())
                .collect(Collectors.toList());
    }

    public TrackingDTO.DailyPhotoTimelineResponse getDailyPhotoTimeline(UUID projectId, LocalDate date) {
        return dailyPhotoTimelineRepository.findByProjectIdAndTimelineDate(projectId, date)
                .map(e -> TrackingDTO.DailyPhotoTimelineResponse.builder()
                        .id(e.getId())
                        .projectId(e.getProjectId())
                        .timelineDate(e.getTimelineDate())
                        .morningPhotoUrl(e.getMorningPhotoUrl())
                        .afternoonPhotoUrl(e.getAfternoonPhotoUrl())
                        .eveningPhotoUrl(e.getEveningPhotoUrl())
                        .summary(e.getSummary())
                        .weatherCondition(e.getWeatherCondition())
                        .build())
                .orElse(null);
    }

    public List<TrackingDTO.WorkCompletionCertificateResponse> getCompletionCertificates(UUID projectId) {
        return certificateRepository.findByProjectIdOrderByCompletionDateDesc(projectId).stream()
                .map(e -> TrackingDTO.WorkCompletionCertificateResponse.builder()
                        .id(e.getId())
                        .projectId(e.getProjectId())
                        .workSection(e.getWorkSection())
                        .completionDate(e.getCompletionDate())
                        .status(e.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}

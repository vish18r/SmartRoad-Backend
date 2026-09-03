package com.nextenti.services.core.mapper.tracking;

import com.nextenti.services.core.dto.tracking.TrackingDTO;
import com.nextenti.services.domain.entity.tracking.*;
import org.springframework.stereotype.Component;

@Component
public class TrackingMappers {

    public TrackingDTO.WorkerCheckinResponse toWorkerCheckinResponse(WorkerCheckinEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.WorkerCheckinResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .workerId(entity.getWorkerId())
                .checkInTime(entity.getCheckInTime())
                .checkInLatitude(entity.getCheckInLatitude())
                .checkInLongitude(entity.getCheckInLongitude())
                .checkOutTime(entity.getCheckOutTime())
                .checkOutLatitude(entity.getCheckOutLatitude())
                .checkOutLongitude(entity.getCheckOutLongitude())
                .status(entity.getStatus())
                .durationMinutes(entity.getDurationMinutes())
                .build();
    }

    public TrackingDTO.QRCodeResponse toQRCodeResponse(QRCodeEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.QRCodeResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .qrCode(entity.getQrCode())
                .qrType(entity.getQrType())
                .referenceType(entity.getReferenceType())
                .referenceId(entity.getReferenceId())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }

    public TrackingDTO.QRScanResponse toQRScanResponse(QRScanEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.QRScanResponse.builder()
                .id(entity.getId())
                .qrCodeId(entity.getQrCodeId())
                .projectId(entity.getProjectId())
                .scannedBy(entity.getScannedBy())
                .scanTime(entity.getScanTime())
                .scanLatitude(entity.getScanLatitude())
                .scanLongitude(entity.getScanLongitude())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .build();
    }

    public TrackingDTO.WorkPhotoResponse toWorkPhotoResponse(WorkPhotoEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.WorkPhotoResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .photoType(entity.getPhotoType())
                .photoUrl(entity.getPhotoUrl())
                .photoDate(entity.getPhotoDate())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .description(entity.getDescription())
                .workArea(entity.getWorkArea())
                .uploadedBy(entity.getUploadedBy())
                .build();
    }

    public TrackingDTO.DailyPhotoTimelineResponse toDailyPhotoTimelineResponse(DailyPhotoTimelineEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.DailyPhotoTimelineResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .timelineDate(entity.getTimelineDate())
                .morningPhotoUrl(entity.getMorningPhotoUrl())
                .afternoonPhotoUrl(entity.getAfternoonPhotoUrl())
                .eveningPhotoUrl(entity.getEveningPhotoUrl())
                .summary(entity.getSummary())
                .weatherCondition(entity.getWeatherCondition())
                .build();
    }

    public TrackingDTO.WorkCompletionCertificateResponse toWorkCompletionCertificateResponse(WorkCompletionCertificateEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.WorkCompletionCertificateResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .workSection(entity.getWorkSection())
                .completionDate(entity.getCompletionDate())
                .completedBy(entity.getCompletedBy())
                .verifiedBy(entity.getVerifiedBy())
                .verificationDate(entity.getVerificationDate())
                .photoUrl(entity.getPhotoUrl())
                .signatureUrl(entity.getSignatureUrl())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .build();
    }

    public TrackingDTO.GeofenceZoneResponse toGeofenceZoneResponse(GeofenceZoneEntity entity) {
        if (entity == null) return null;
        return TrackingDTO.GeofenceZoneResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .name(entity.getName())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .radiusMeters(entity.getRadiusMeters())
                .isActive(entity.getIsActive())
                .build();
    }
}

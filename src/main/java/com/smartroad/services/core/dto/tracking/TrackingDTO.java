package com.smartroad.services.core.dto.tracking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TrackingDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkerCheckinRequest { @NotNull public UUID projectId; @NotNull public BigDecimal latitude; @NotNull public BigDecimal longitude; public BigDecimal accuracy; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkerCheckinResponse { public UUID id; public UUID projectId; public UUID workerId; public OffsetDateTime checkInTime; public BigDecimal checkInLatitude; public BigDecimal checkInLongitude; public OffsetDateTime checkOutTime; public BigDecimal checkOutLatitude; public BigDecimal checkOutLongitude; public String status; public Integer durationMinutes; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QRCodeRequest { @NotNull public UUID projectId; @NotBlank public String qrCode; @NotBlank public String qrType; @NotBlank public String referenceType; @NotNull public UUID referenceId; public String description; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QRCodeResponse { public UUID id; public UUID projectId; public String qrCode; public String qrType; public String referenceType; public UUID referenceId; public String description; public Boolean isActive; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QRScanRequest { @NotNull public UUID projectId; @NotBlank public String qrCode; public BigDecimal latitude; public BigDecimal longitude; public String notes; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QRScanResponse { public UUID id; public UUID qrCodeId; public UUID projectId; public UUID scannedBy; public OffsetDateTime scanTime; public BigDecimal scanLatitude; public BigDecimal scanLongitude; public String status; public String notes; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkPhotoRequest { @NotNull public UUID projectId; @NotBlank public String photoType; @NotBlank public String photoUrl; public BigDecimal latitude; public BigDecimal longitude; public String description; public String workArea; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkPhotoResponse { public UUID id; public UUID projectId; public String photoType; public String photoUrl; public OffsetDateTime photoDate; public BigDecimal latitude; public BigDecimal longitude; public String description; public String workArea; public UUID uploadedBy; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailyPhotoTimelineRequest { @NotNull public UUID projectId; @NotNull public LocalDate timelineDate; public String morningPhotoUrl; public String afternoonPhotoUrl; public String eveningPhotoUrl; public String summary; public String weatherCondition; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailyPhotoTimelineResponse { public UUID id; public UUID projectId; public LocalDate timelineDate; public String morningPhotoUrl; public String afternoonPhotoUrl; public String eveningPhotoUrl; public String summary; public String weatherCondition; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkCompletionCertificateRequest { @NotNull public UUID projectId; @NotBlank public String workSection; public String photoUrl; public String signatureUrl; public String remarks; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkCompletionCertificateResponse { public UUID id; public UUID projectId; public String workSection; public OffsetDateTime completionDate; public UUID completedBy; public UUID verifiedBy; public OffsetDateTime verificationDate; public String photoUrl; public String signatureUrl; public String status; public String remarks; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeofenceZoneRequest { @NotNull public UUID projectId; @NotBlank public String name; @NotNull public BigDecimal latitude; @NotNull public BigDecimal longitude; @NotNull public BigDecimal radiusMeters; }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GeofenceZoneResponse { public UUID id; public UUID projectId; public String name; public BigDecimal latitude; public BigDecimal longitude; public BigDecimal radiusMeters; public Boolean isActive; }
}

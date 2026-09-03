package com.nextenti.services.core.dto.tracking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class QCAndSafetyDTOs {

    // QC Checklist
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QCChecklistRequest {
        @NotNull public UUID projectId;
        @NotBlank public String checklistName;
        public String description;
        public List<String> checkItems;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class QCChecklistResponse {
        public UUID id;
        public UUID projectId;
        public String checklistName;
        public String description;
        public List<String> checkItems;
        public String status;
        public OffsetDateTime createdDate;
    }

    // Concrete Cube Test
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConcreteCubeTestRequest {
        @NotNull public UUID projectId;
        @NotBlank public String testReference;
        @NotNull public Integer daysCured;
        @NotNull public Double compressiveStrength;
        public String batchNumber;
        public String location;
        public String remarks;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ConcreteCubeTestResponse {
        public UUID id;
        public UUID projectId;
        public String testReference;
        public Integer daysCured;
        public Double compressiveStrength;
        public String batchNumber;
        public String location;
        public String status;
        public String remarks;
        public OffsetDateTime testDate;
    }

    // Material Lab Test
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MaterialLabTestRequest {
        @NotNull public UUID projectId;
        @NotBlank public String materialType;
        @NotBlank public String testType;
        public String labName;
        public String reportUrl;
        public String result;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MaterialLabTestResponse {
        public UUID id;
        public UUID projectId;
        public String materialType;
        public String testType;
        public String labName;
        public String reportUrl;
        public String result;
        public String status;
        public OffsetDateTime testDate;
    }

    // Safety Incident
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SafetyIncidentRequest {
        @NotNull public UUID projectId;
        @NotBlank public String incidentType;
        public String description;
        public String severity;
        public UUID reportedBy;
        public String photoUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SafetyIncidentResponse {
        public UUID id;
        public UUID projectId;
        public String incidentType;
        public String description;
        public String severity;
        public UUID reportedBy;
        public String photoUrl;
        public String status;
        public OffsetDateTime incidentDate;
    }

    // PPE Compliance
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PPEComplianceRequest {
        @NotNull public UUID projectId;
        @NotNull public UUID workerId;
        public Boolean hardhat;
        public Boolean safetyVest;
        public Boolean safetyBoots;
        public Boolean gloves;
        public Boolean eyeProtection;
        public String remarks;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PPEComplianceResponse {
        public UUID id;
        public UUID projectId;
        public UUID workerId;
        public Boolean hardhat;
        public Boolean safetyVest;
        public Boolean safetyBoots;
        public Boolean gloves;
        public Boolean eyeProtection;
        public String remarks;
        public Integer complianceScore;
        public OffsetDateTime checkDate;
    }

    // Defect/Snag
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DefectRequest {
        @NotNull public UUID projectId;
        @NotBlank public String defectType;
        public String description;
        public String severity;
        public String location;
        public String photoUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DefectResponse {
        public UUID id;
        public UUID projectId;
        public String defectType;
        public String description;
        public String severity;
        public String location;
        public String photoUrl;
        public String status;
        public OffsetDateTime reportedDate;
    }

    // Material Wastage
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MaterialWastageRequest {
        @NotNull public UUID projectId;
        @NotBlank public String materialType;
        @NotNull public Double quantityWasted;
        public String unit;
        public String reason;
        public String photoUrl;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MaterialWastageResponse {
        public UUID id;
        public UUID projectId;
        public String materialType;
        public Double quantityWasted;
        public String unit;
        public String reason;
        public String photoUrl;
        public OffsetDateTime reportedDate;
    }

    // Customer Complaint
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerComplaintRequest {
        @NotNull public UUID projectId;
        @NotBlank public String complaintType;
        public String description;
        public String priority;
        public String contactInfo;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CustomerComplaintResponse {
        public UUID id;
        public UUID projectId;
        public String complaintType;
        public String description;
        public String priority;
        public String contactInfo;
        public String status;
        public OffsetDateTime registeredDate;
    }

    // Daily Site Report
    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailySiteReportRequest {
        @NotNull public UUID projectId;
        public String weatherCondition;
        public Integer workersPresent;
        public String workCompletedToday;
        public String plannedForTomorrow;
        public Integer safetyIncidents;
        public String remarks;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DailySiteReportResponse {
        public UUID id;
        public UUID projectId;
        public String weatherCondition;
        public Integer workersPresent;
        public String workCompletedToday;
        public String plannedForTomorrow;
        public Integer safetyIncidents;
        public String remarks;
        public String reportUrl;
        public OffsetDateTime reportDate;
    }
}

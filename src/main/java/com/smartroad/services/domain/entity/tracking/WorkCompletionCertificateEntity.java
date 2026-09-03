package com.smartroad.services.domain.entity.tracking;

import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing work completion certificates in the sr_work_completion_certificates table.
 * Digital work completion verification with signatures and photos.
 */
@Entity
@Table(name = "sr_work_completion_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WorkCompletionCertificateEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "work_section", nullable = false)
    private String workSection;

    @Column(name = "completion_date", nullable = false)
    private OffsetDateTime completionDate;

    @Column(name = "completed_by", nullable = false)
    private UUID completedBy;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verification_date")
    private OffsetDateTime verificationDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "remarks")
    private String remarks;
}

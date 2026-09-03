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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing work photos in the sr_work_photos table.
 * Stores project evidence photos (before/after, daily timeline).
 */
@Entity
@Table(name = "sr_work_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class WorkPhotoEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "photo_type", nullable = false)
    private String photoType;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "photo_date", nullable = false)
    private OffsetDateTime photoDate;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "description")
    private String description;

    @Column(name = "work_area")
    private String workArea;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;
}

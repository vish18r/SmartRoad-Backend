package com.smartroad.services.domain.entity.sitediary;

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
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing a daily site diary entry stored in the sr_site_diary table.
 */
@Entity
@Table(name = "sr_site_diary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SiteDiaryEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    @Column(name = "weather")
    private String weather;

    @Column(name = "temperature_celsius")
    private BigDecimal temperatureCelsius;

    @Column(name = "site_conditions", columnDefinition = "TEXT")
    private String siteConditions;

    @Column(name = "work_summary", columnDefinition = "TEXT")
    private String workSummary;

    @Column(name = "issues", columnDefinition = "TEXT")
    private String issues;

    @Column(name = "safety_notes", columnDefinition = "TEXT")
    private String safetyNotes;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

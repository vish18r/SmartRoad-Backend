package com.smartroad.services.domain.entity.tracking;

import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing daily photo timelines in the sr_daily_photo_timelines table.
 * Stores morning, afternoon, evening photos for each project day.
 */
@Entity
@Table(name = "sr_daily_photo_timelines", uniqueConstraints = {@UniqueConstraint(columnNames = {"project_id", "timeline_date"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DailyPhotoTimelineEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "timeline_date", nullable = false)
    private LocalDate timelineDate;

    @Column(name = "morning_photo_url")
    private String morningPhotoUrl;

    @Column(name = "afternoon_photo_url")
    private String afternoonPhotoUrl;

    @Column(name = "evening_photo_url")
    private String eveningPhotoUrl;

    @Column(name = "summary")
    private String summary;

    @Column(name = "weather_condition")
    private String weatherCondition;
}

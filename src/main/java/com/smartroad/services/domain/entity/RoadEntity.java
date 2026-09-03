package com.smartroad.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity representing a road stored in the roads table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_roads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class RoadEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(name = "length_m")
    private BigDecimal lengthM;

    @Column(name = "width_m")
    private BigDecimal widthM;

    @Column(name = "thickness_mm")
    private BigDecimal thicknessMm;

    @Column(name = "start_chainage")
    private String startChainage;

    @Column(name = "end_chainage")
    private String endChainage;

    @Column(name = "completed_length_m")
    private BigDecimal completedLengthM;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "archived")
    private Boolean archived;
}


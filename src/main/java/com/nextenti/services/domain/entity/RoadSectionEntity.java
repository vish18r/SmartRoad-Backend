package com.nextenti.services.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "road_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoadSectionEntity extends SmartRoadBaseEntity {

    @Column(name = "road_id", nullable = false)
    private UUID roadId;

    @Column(name = "section_number")
    private Integer sectionNumber;

    @Column(name = "start_km")
    private Double startKm;

    @Column(name = "end_km")
    private Double endKm;

    @Column(name = "length")
    private Double length;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private String status;
}


package com.nextenti.services.domain.entity;

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
 * JPA entity representing a road section stored in the road_sections table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_road_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class RoadSectionEntity extends SmartRoadBaseEntity {

    @Column(name = "road_id", nullable = false)
    private UUID roadId;

    @Column(name = "section_number")
    private Integer sectionNumber;

    @Column(name = "start_chainage")
    private String startChainage;

    @Column(name = "end_chainage")
    private String endChainage;

    @Column(name = "length_m")
    private BigDecimal lengthM;

    @Column(name = "completed_length_m")
    private BigDecimal completedLengthM;

    @Column(name = "status")
    private String status;
}


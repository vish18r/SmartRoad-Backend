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
@Table(name = "roads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoadEntity extends SmartRoadBaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_length")
    private Double totalLength;

    @Column(name = "width")
    private Double width;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private String status;
}


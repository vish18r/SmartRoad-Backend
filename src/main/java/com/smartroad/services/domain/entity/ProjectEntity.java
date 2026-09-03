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
import java.util.Date;
import java.util.UUID;

/**
 * JPA entity representing a project stored in the projects table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProjectEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "code")
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "budget")
    private BigDecimal budget;

    @Column(name = "actual_cost")
    private BigDecimal actualCost;

    @Column(name = "progress")
    private BigDecimal progress;

    @Column(name = "status")
    private String status;

    @Column(name = "archived")
    private Boolean archived;
}


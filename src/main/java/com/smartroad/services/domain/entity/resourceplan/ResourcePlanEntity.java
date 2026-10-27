package com.smartroad.services.domain.entity.resourceplan;

import com.smartroad.services.common.enums.resourceplan.ResourcePlanStatusEnum;
import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
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

import java.time.LocalDate;
import java.util.UUID;

/**
 * JPA entity representing a resource plan stored in the sr_resource_plans table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_resource_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ResourcePlanEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "supervisor_worker_id")
    private UUID supervisorWorkerId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourcePlanStatusEnum status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

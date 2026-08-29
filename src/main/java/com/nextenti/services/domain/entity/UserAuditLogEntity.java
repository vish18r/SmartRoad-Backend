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
import java.util.UUID;

/**
 * @author Rishikesh
 * @version 1.0
 */
@Entity
@Table(name = "sr_user_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserAuditLogEntity extends SmartRoadBaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "action_done")
    private String actionDone;

    @Column(name = "nt_status")
    private String ntStatus;

    @Column(name = "reason")
    private String reason;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "pending_user_yn")
    private Boolean pendingUserYn;
}


package com.smartroad.services.domain.entity.client;

import com.smartroad.services.domain.entity.SmartRoadBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * JPA entity representing a client stored in the clients table.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ClientEntity extends SmartRoadBaseEntity {

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "active")
    private Boolean active;
}


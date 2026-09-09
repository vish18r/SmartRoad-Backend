package com.smartroad.services.domain.entity.company;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;
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

import java.util.UUID;

/**
 * JPA entity representing a company contact (founder or additional contact)
 * stored in the sr_company_contacts table, associated with a
 * {@link CompanyProfileEntity}. Reuses {@link ContactRoleEnum} from the
 * business-profile domain since the role values (main founder, additional
 * contact) are identical.
 *
 * @author Vishal
 * @version 1.0
 */
@Entity
@Table(name = "sr_company_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CompanyContactEntity extends SmartRoadBaseEntity {

    @Column(name = "company_profile_id", nullable = false)
    private UUID companyProfileId;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContactRoleEnum contactRole;

    @Column(name = "phone_number_1")
    private String phoneNumber1;

    @Column(name = "phone_number_2")
    private String phoneNumber2;
}

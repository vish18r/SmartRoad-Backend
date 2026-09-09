package com.smartroad.services.core.service.company;

import com.smartroad.services.common.enums.businessprofile.ContactRoleEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.company.CompanyContactResponseDTO;
import com.smartroad.services.core.dto.company.CompanyServiceResponseDTO;
import com.smartroad.services.core.dto.company.CompleteCompanyProfileResponseDTO;
import com.smartroad.services.core.mapper.company.CompanyContactMapper;
import com.smartroad.services.core.mapper.company.CompanyProfileMapper;
import com.smartroad.services.core.mapper.company.CompanyServiceMapper;
import com.smartroad.services.domain.entity.company.CompanyContactEntity;
import com.smartroad.services.domain.entity.company.CompanyProfileEntity;
import com.smartroad.services.domain.entity.company.CompanyServiceEntity;
import com.smartroad.services.domain.repository.CompanyContactRepository;
import com.smartroad.services.domain.repository.CompanyProfileRepository;
import com.smartroad.services.domain.repository.CompanyServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for public company profile business logic.
 * Coordinates between repositories and mappers to assemble the complete,
 * publicly-visible company profile (brand details, founder, additional
 * contacts, and services).
 */
@Service
public class CompanyProfileService {

    /**
     * The company profile is a single, application-wide record (unlike
     * per-organization business profiles), seeded by database migration under
     * this fixed ID.
     */
    private static final UUID COMPANY_PROFILE_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyServiceRepository companyServiceRepository;
    private final CompanyProfileMapper companyProfileMapper;
    private final CompanyContactMapper companyContactMapper;
    private final CompanyServiceMapper companyServiceMapper;

    /**
     * Constructs the service with required dependencies.
     *
     * @param companyProfileRepository repository for the company profile
     * @param companyContactRepository repository for company contacts
     * @param companyServiceRepository repository for company services
     * @param companyProfileMapper mapper for the company profile
     * @param companyContactMapper mapper for company contacts
     * @param companyServiceMapper mapper for company services
     */
    public CompanyProfileService(
        CompanyProfileRepository companyProfileRepository,
        CompanyContactRepository companyContactRepository,
        CompanyServiceRepository companyServiceRepository,
        CompanyProfileMapper companyProfileMapper,
        CompanyContactMapper companyContactMapper,
        CompanyServiceMapper companyServiceMapper
    ) {
        this.companyProfileRepository = companyProfileRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyServiceRepository = companyServiceRepository;
        this.companyProfileMapper = companyProfileMapper;
        this.companyContactMapper = companyContactMapper;
        this.companyServiceMapper = companyServiceMapper;
    }

    /**
     * Retrieves the complete public company profile, including the founder,
     * additional contacts, and services, for unauthenticated display.
     *
     * @return the complete company profile response DTO
     * @throws SmartRoadException if the company profile has not been seeded
     */
    @Transactional(readOnly = true)
    public CompleteCompanyProfileResponseDTO getPublicCompanyProfile() throws SmartRoadException {
        CompanyProfileEntity profile = companyProfileRepository.findById(COMPANY_PROFILE_ID)
            .orElseThrow(() -> new SmartRoadException(
                ApplicationLayer.SERVICE_LAYER,
                ErrorCodeMapping.DAO_NOT_FOUND,
                "companyprofile.not.found"
            ));

        List<CompanyContactEntity> contacts = companyContactRepository.findByCompanyProfileId(profile.getId());
        List<CompanyServiceEntity> services = companyServiceRepository.findByCompanyProfileIdOrderByDisplayOrderAsc(profile.getId());

        CompanyContactResponseDTO founder = contacts.stream()
            .filter(contact -> contact.getContactRole() == ContactRoleEnum.MAIN_FOUNDER)
            .findFirst()
            .map(companyContactMapper::toResponseDTO)
            .orElse(null);

        List<CompanyContactResponseDTO> additionalContacts = contacts.stream()
            .filter(contact -> contact.getContactRole() == ContactRoleEnum.ADDITIONAL_CONTACT)
            .map(companyContactMapper::toResponseDTO)
            .collect(Collectors.toList());

        List<CompanyServiceResponseDTO> serviceDTOs = services.stream()
            .map(companyServiceMapper::toResponseDTO)
            .collect(Collectors.toList());

        var profileDTO = companyProfileMapper.toResponseDTO(profile);

        return new CompleteCompanyProfileResponseDTO(
            profileDTO.id(),
            profileDTO.brandName(),
            profileDTO.businessName(),
            profileDTO.businessType(),
            profileDTO.productName(),
            profileDTO.tagline(),
            profileDTO.phone(),
            profileDTO.address(),
            founder,
            additionalContacts,
            serviceDTOs
        );
    }
}

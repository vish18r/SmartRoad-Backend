package com.smartroad.services.core.service.organization;

import com.smartroad.services.common.enums.UserRole;
import com.smartroad.services.common.exception.*;
import com.smartroad.services.core.dto.organization.*;
import com.smartroad.services.domain.entity.organization.OrganizationEntity;
import com.smartroad.services.domain.entity.organization.OrganizationMemberEntity;
import com.smartroad.services.domain.repository.OrganizationMemberRepository;
import com.smartroad.services.domain.repository.OrganizationRepository;
import com.smartroad.services.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrganizationService {
    private final OrganizationRepository organizations;
    private final OrganizationMemberRepository members;
    private final UserRepository users;

    public OrganizationService(OrganizationRepository organizations, OrganizationMemberRepository members, UserRepository users) {
        this.organizations = organizations;
        this.members = members;
        this.users = users;
    }

    @Transactional
    public OrganizationResponseDTO create(UUID userId, OrganizationRequestDTO request) {
        OrganizationEntity organization = new OrganizationEntity();
        apply(organization, request);
        organizations.save(organization);

        OrganizationMemberEntity owner = new OrganizationMemberEntity();
        owner.setOrganizationId(organization.getId());
        owner.setUserId(userId);
        owner.setRole(UserRole.ADMIN.getValue());
        members.save(owner);
        return toResponse(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> mine(UUID userId) {
        return members.findByUserIdAndActiveTrue(userId).stream()
                .map(OrganizationMemberEntity::getOrganizationId)
                .map(id -> organizations.findById(id).orElse(null))
                .filter(Objects::nonNull).filter(o -> Boolean.TRUE.equals(o.getActive()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponseDTO get(UUID userId, UUID organizationId) throws SmartRoadException {
        requireMember(userId, organizationId);
        return toResponse(find(organizationId));
    }

    @Transactional
    public OrganizationResponseDTO update(UUID userId, UUID organizationId, OrganizationRequestDTO request) throws SmartRoadException {
        requireAdmin(userId, organizationId);
        OrganizationEntity organization = find(organizationId);
        apply(organization, request);
        organization.setModifiedBy(userId);
        return toResponse(organizations.save(organization));
    }

    @Transactional
    public void deactivate(UUID userId, UUID organizationId) throws SmartRoadException {
        requireAdmin(userId, organizationId);
        OrganizationEntity organization = find(organizationId);
        organization.setActive(false);
        organization.setModifiedBy(userId);
        organizations.save(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponseDTO> listMembers(UUID userId, UUID organizationId) throws SmartRoadException {
        requireMember(userId, organizationId);
        return members.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public OrganizationMemberResponseDTO addMember(UUID actorId, UUID organizationId, OrganizationMemberRequestDTO request) throws SmartRoadException {
        requireAdmin(actorId, organizationId);
        if (!users.existsById(request.userId())) throw notFound("user.not.found");
        OrganizationMemberEntity member = members.findByOrganizationIdAndUserId(organizationId, request.userId()).orElse(null);
        if (member == null) {
            member = new OrganizationMemberEntity();
            member.setOrganizationId(organizationId);
            member.setUserId(request.userId());
        }
        member.setRole(request.role().getValue());
        member.setActive(true);
        member.setModifiedBy(actorId);
        return toMemberResponse(members.save(member));
    }

    @Transactional
    public void removeMember(UUID actorId, UUID organizationId, UUID memberUserId) throws SmartRoadException {
        requireAdmin(actorId, organizationId);
        OrganizationMemberEntity member = requireMember(memberUserId, organizationId);
        if (member.getUserId().equals(actorId)) throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "organization.owner.cannot.remove.self");
        member.setActive(false);
        member.setModifiedBy(actorId);
        members.save(member);
    }

    /**
     * Retrieves all organizations.
     *
     * @return a list of all organizations
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> list() {
        return organizations.findAll().stream()
                .filter(o -> Boolean.TRUE.equals(o.getActive()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves all organizations for the authenticated user.
     *
     * @return a list of user's organizations
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> getMyOrganizations() {
        // This should be called with userId from SecurityContext
        // For now, returning empty list as placeholder
        return new ArrayList<>();
    }

    /**
     * Deletes (deactivates) an organization by ID.
     *
     * @param organizationId the UUID of the organization to delete
     * @throws SmartRoadException if organization not found
     */
    @Transactional
    public void delete(UUID organizationId) throws SmartRoadException {
        OrganizationEntity organization = find(organizationId);
        organization.setActive(false);
        organizations.save(organization);
    }

    /**
     * Maps an organization member entity to its response DTO.
     *
     * @param member the organization member entity
     * @return the organization member response DTO
     */
    private OrganizationMemberResponseDTO toMemberResponse(OrganizationMemberEntity member) {
        return new OrganizationMemberResponseDTO(member.getUserId(), UserRole.valueOf(member.getRole()),
                Boolean.TRUE.equals(member.getActive()));
    }

    /**
     * Resolves the organization a request should act on when the client did not supply one.
     * Returns the first organization the user is an active member of.
     *
     * @param userId the authenticated user's UUID
     * @return the resolved organization UUID
     * @throws SmartRoadException if the user is not an active member of any organization
     */
    @Transactional(readOnly = true)
    public UUID resolveDefaultOrganizationId(UUID userId) throws SmartRoadException {
        return members.findByUserIdAndActiveTrue(userId).stream()
                .map(OrganizationMemberEntity::getOrganizationId)
                .findFirst()
                .orElseThrow(() -> notFound("organization.membership.not.found"));
    }

    public OrganizationMemberEntity requireMember(UUID userId, UUID organizationId) throws SmartRoadException {
        return members.findByOrganizationIdAndUserIdAndActiveTrue(organizationId, userId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "organization.access.denied"));
    }

    private void requireAdmin(UUID userId, UUID organizationId) throws SmartRoadException {
        String role = requireMember(userId, organizationId).getRole();
        if (!UserRole.ADMIN.getValue().equals(role))
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "organization.admin.required");
    }

    private OrganizationEntity find(UUID id) throws SmartRoadException {
        return organizations.findById(id).orElseThrow(() -> notFound("organization.not.found"));
    }
    private SmartRoadException notFound(String key) { return new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, key); }
    private void apply(OrganizationEntity organization, OrganizationRequestDTO r) {
        organization.setName(r.name()); organization.setLegalName(r.legalName()); organization.setGstNumber(r.gstNumber());
        organization.setEmail(r.email()); organization.setPhoneNumber(r.phoneNumber()); organization.setAddress(r.address()); organization.setLogoUrl(r.logoUrl());
    }
    private OrganizationResponseDTO toResponse(OrganizationEntity o) { return new OrganizationResponseDTO(o.getId(), o.getName(), o.getLegalName(), o.getGstNumber(), o.getEmail(), o.getPhoneNumber(), o.getAddress(), o.getLogoUrl(), Boolean.TRUE.equals(o.getActive())); }
}

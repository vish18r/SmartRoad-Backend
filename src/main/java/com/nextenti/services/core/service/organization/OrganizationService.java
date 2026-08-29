package com.nextenti.services.core.service.organization;

import com.nextenti.services.common.enums.UserRole;
import com.nextenti.services.common.exception.*;
import com.nextenti.services.core.dto.organization.*;
import com.nextenti.services.domain.entity.OrganizationEntity;
import com.nextenti.services.domain.entity.OrganizationMemberEntity;
import com.nextenti.services.domain.repository.OrganizationMemberRepository;
import com.nextenti.services.domain.repository.OrganizationRepository;
import com.nextenti.services.domain.repository.UserRepository;
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
    public OrganizationResponse create(UUID userId, OrganizationRequest request) {
        OrganizationEntity organization = new Organization();
        apply(organization, request);
        organization.setId(UUID.randomUUID());
        organization.setCreatedBy(userId);
        organization.setModifiedBy(userId);
        organizations.save(organization);

        OrganizationMemberEntity owner = new OrganizationMember();
        owner.setId(UUID.randomUUID());
        owner.setOrganizationId(organization.getId());
        owner.setUserId(userId);
        owner.setRole(UserRole.ADMIN);
        owner.setCreatedBy(userId);
        owner.setModifiedBy(userId);
        members.save(owner);
        return toResponse(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> mine(UUID userId) {
        return members.findByUserIdAndActiveTrue(userId).stream()
                .map(OrganizationMember::getOrganizationId)
                .map(id -> organizations.findById(id).orElse(null))
                .filter(Objects::nonNull).filter(o -> Boolean.TRUE.equals(o.getActive()))
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse get(UUID userId, UUID organizationId) throws SmartRoadException {
        requireMember(userId, organizationId);
        return toResponse(find(organizationId));
    }

    @Transactional
    public OrganizationResponse update(UUID userId, UUID organizationId, OrganizationRequest request) throws SmartRoadException {
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
    public List<OrganizationMemberResponse> listMembers(UUID userId, UUID organizationId) throws SmartRoadException {
        requireMember(userId, organizationId);
        return members.findByOrganizationIdAndActiveTrue(organizationId).stream()
                .map(member -> new OrganizationMemberResponse(member.getUserId(), member.getRole(), Boolean.TRUE.equals(member.getActive())))
                .toList();
    }

    @Transactional
    public void addMember(UUID actorId, UUID organizationId, OrganizationMemberRequest request) throws SmartRoadException {
        requireAdmin(actorId, organizationId);
        if (!users.existsById(request.userId())) throw notFound("user.not.found");
        OrganizationMemberEntity member = members.findByOrganizationIdAndUserIdAndActiveTrue(organizationId, request.userId()).orElse(null);
        if (member == null) {
            member = new OrganizationMember();
            member.setId(UUID.randomUUID());
            member.setOrganizationId(organizationId);
            member.setUserId(request.userId());
            member.setCreatedBy(actorId);
        }
        member.setRole(request.role());
        member.setActive(true);
        member.setModifiedBy(actorId);
        members.save(member);
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

    public OrganizationMemberEntity requireMember(UUID userId, UUID organizationId) throws SmartRoadException {
        return members.findByOrganizationIdAndUserIdAndActiveTrue(organizationId, userId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "organization.access.denied"));
    }

    private void requireAdmin(UUID userId, UUID organizationId) throws SmartRoadException {
        if (requireMember(userId, organizationId).getRole() != UserRole.ADMIN)
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_ACCESS_DENIED, "organization.admin.required");
    }

    private OrganizationEntity find(UUID id) throws SmartRoadException {
        return organizations.findById(id).orElseThrow(() -> notFound("organization.not.found"));
    }
    private SmartRoadException notFound(String key) { return new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, key); }
    private void apply(OrganizationEntity organization, OrganizationRequest r) {
        organization.setName(r.name()); organization.setLegalName(r.legalName()); organization.setGstNumber(r.gstNumber());
        organization.setEmail(r.email()); organization.setPhoneNumber(r.phoneNumber()); organization.setAddress(r.address()); organization.setLogoUrl(r.logoUrl());
    }
    private OrganizationResponse toResponse(OrganizationEntity o) { return new OrganizationResponse(o.getId(), o.getName(), o.getLegalName(), o.getGstNumber(), o.getEmail(), o.getPhoneNumber(), o.getAddress(), o.getLogoUrl(), Boolean.TRUE.equals(o.getActive())); }
}

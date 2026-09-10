package com.smartroad.services.core.service.project;

import com.smartroad.services.common.enums.ProjectStatus;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.project.ProjectRequestDTO;
import com.smartroad.services.core.dto.project.ProjectResponseDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for project business logic.
 * Manages project creation, retrieval, updating, and archival operations.
 */
@Service
public class ProjectService {

    private final ProjectRepository projects;
    private final OrganizationService orgs;

    /**
     * Constructs a ProjectService with required dependencies.
     *
     * @param p the project repository
     * @param o the organization service
     */
    public ProjectService(ProjectRepository p, OrganizationService o) {
        projects = p;
        orgs = o;
    }

    /**
     * Creates a new project.
     *
     * @param u the user UUID
     * @param r the project request DTO
     * @return the created project response DTO
     * @throws SmartRoadException if user is not an organization member
     */
    @Transactional
    public ProjectResponseDTO create(UUID u, ProjectRequestDTO r) throws SmartRoadException {
        orgs.requireMember(u, r.organizationId());
        ProjectEntity p = new ProjectEntity();
        p.setOrganizationId(r.organizationId());
        apply(p, r);
        return map(projects.save(p));
    }

    /**
     * Retrieves all non-archived projects for an organization.
     *
     * @param u the user UUID
     * @param org the organization UUID
     * @return list of project response DTOs
     * @throws SmartRoadException if user is not an organization member
     */
    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> list(UUID u, UUID org) throws SmartRoadException {
        orgs.requireMember(u, org);
        return projects.findByOrganizationIdAndArchivedFalse(org).stream().map(this::map).toList();
    }

    /**
     * Retrieves a specific project by ID without authorization check.
     *
     * @param id the project UUID
     * @return the project response DTO
     * @throws SmartRoadException if project not found
     */
    @Transactional(readOnly = true)
    public ProjectResponseDTO getById(UUID id) throws SmartRoadException {
        ProjectEntity p = projects.findById(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found"));
        return map(p);
    }

    /**
     * Retrieves a specific project.
     *
     * @param u the user UUID
     * @param id the project UUID
     * @param org the organization UUID
     * @return the project response DTO
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional(readOnly = true)
    public ProjectResponseDTO get(UUID u, UUID id, UUID org) throws SmartRoadException {
        return map(find(u, id, org));
    }

    /**
     * Updates an existing project.
     *
     * @param u the user UUID
     * @param id the project UUID
     * @param r the project request DTO
     * @return the updated project response DTO
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional
    public ProjectResponseDTO update(UUID u, UUID id, ProjectRequestDTO r) throws SmartRoadException {
        ProjectEntity p = find(u, id, r.organizationId());
        apply(p, r);
        p.setModifiedBy(u);
        return map(projects.save(p));
    }

    /**
     * Archives a project (soft delete).
     *
     * @param u the user UUID
     * @param id the project UUID
     * @param org the organization UUID
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional
    public void archive(UUID u, UUID id, UUID org) throws SmartRoadException {
        ProjectEntity p = find(u, id, org);
        p.setArchived(true);
        p.setModifiedBy(u);
        projects.save(p);
    }

    /**
     * Archives a project (soft delete) resolving the owning organization from the project itself.
     * Used by the {@code DELETE /projects/{id}} endpoint, which does not carry an organization ID.
     *
     * @param u the user UUID
     * @param id the project UUID
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional
    public void archive(UUID u, UUID id) throws SmartRoadException {
        ProjectEntity p = projects.findById(id)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found"));
        orgs.requireMember(u, p.getOrganizationId());
        p.setArchived(true);
        p.setModifiedBy(u);
        projects.save(p);
    }

    /**
     * Finds a project with authorization check.
     *
     * @param u the user UUID
     * @param id the project UUID
     * @param org the organization UUID
     * @return the ProjectEntity
     * @throws SmartRoadException if project not found or user not authorized
     */
    private ProjectEntity find(UUID u, UUID id, UUID org) throws SmartRoadException {
        orgs.requireMember(u, org);
        return projects.findByIdAndOrganizationIdAndArchivedFalse(id, org)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found"));
    }

    /**
     * Applies request data to project entity.
     *
     * @param p the project entity to update
     * @param r the project request DTO
     */
    private void apply(ProjectEntity p, ProjectRequestDTO r) {
        p.setClientId(r.clientId());
        p.setCode(r.code());
        p.setName(r.name());
        p.setDescription(r.description());
        p.setLocation(r.location());
        p.setStatus(r.status().name());
        p.setBudget(r.budget());
        p.setProgress(r.progress() == null ? BigDecimal.ZERO : r.progress());
        p.setStartDate(r.startDate() == null ? null : Date.valueOf(r.startDate()));
        p.setEndDate(r.endDate() == null ? null : Date.valueOf(r.endDate()));
    }

    /**
     * Maps project entity to response DTO.
     *
     * @param p the project entity
     * @return the project response DTO
     */
    private ProjectResponseDTO map(ProjectEntity p) {
        LocalDate startDate = p.getStartDate() == null ? null : p.getStartDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = p.getEndDate() == null ? null : p.getEndDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return new ProjectResponseDTO(p.getId(), p.getOrganizationId(), p.getClientId(), p.getCode(), p.getName(),
                p.getDescription(), p.getLocation(), ProjectStatus.valueOf(p.getStatus()), p.getBudget(), p.getActualCost(), p.getProgress(),
                startDate, endDate, Boolean.TRUE.equals(p.getArchived()));
    }
}

package com.nextenti.services.core.service.road;

import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.road.RoadRequestDTO;
import com.nextenti.services.core.dto.road.RoadResponseDTO;
import com.nextenti.services.core.dto.road.RoadSectionRequestDTO;
import com.nextenti.services.core.dto.road.RoadSectionResponseDTO;
import com.nextenti.services.core.service.organization.OrganizationService;
import com.nextenti.services.domain.entity.ProjectEntity;
import com.nextenti.services.domain.entity.RoadEntity;
import com.nextenti.services.domain.entity.RoadSectionEntity;
import com.nextenti.services.domain.repository.ProjectRepository;
import com.nextenti.services.domain.repository.RoadRepository;
import com.nextenti.services.domain.repository.RoadSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for road and road section business logic.
 * Manages road creation, retrieval, updating, deletion, and section management operations.
 */
@Service
public class RoadService {

    private final RoadRepository roads;
    private final RoadSectionRepository sections;
    private final ProjectRepository projects;
    private final OrganizationService orgs;

    /**
     * Constructs a RoadService with required dependencies.
     *
     * @param r the road repository
     * @param s the road section repository
     * @param p the project repository
     * @param o the organization service
     */
    public RoadService(RoadRepository r, RoadSectionRepository s, ProjectRepository p, OrganizationService o) {
        roads = r;
        sections = s;
        projects = p;
        orgs = o;
    }

    /**
     * Creates a new road for a project.
     *
     * @param u the user UUID
     * @param projectId the project UUID
     * @param r the road request DTO
     * @return the created road response DTO
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional
    public RoadResponseDTO create(UUID u, UUID projectId, RoadRequestDTO r) throws SmartRoadException {
        ProjectEntity p = project(u, projectId);
        RoadEntity x = new RoadEntity();
        x.setProjectId(p.getId());
        apply(x, r);
        return map(roads.save(x));
    }

    /**
     * Retrieves all roads for a project.
     *
     * @param u the user UUID
     * @param projectId the project UUID
     * @return list of road response DTOs
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional(readOnly = true)
    public List<RoadResponseDTO> list(UUID u, UUID projectId) throws SmartRoadException {
        project(u, projectId);
        return roads.findByProjectId(projectId).stream().map(this::map).toList();
    }

    /**
     * Retrieves a specific road.
     *
     * @param u the user UUID
     * @param id the road UUID
     * @return the road response DTO
     * @throws SmartRoadException if road not found or user not authorized
     */
    @Transactional(readOnly = true)
    public RoadResponseDTO get(UUID u, UUID id) throws SmartRoadException {
        RoadEntity r = road(id);
        project(u, r.getProjectId());
        return map(r);
    }

    /**
     * Updates an existing road.
     *
     * @param u the user UUID
     * @param id the road UUID
     * @param r the road request DTO
     * @return the updated road response DTO
     * @throws SmartRoadException if road not found or user not authorized
     */
    @Transactional
    public RoadResponseDTO update(UUID u, UUID id, RoadRequestDTO r) throws SmartRoadException {
        RoadEntity x = road(id);
        project(u, x.getProjectId());
        apply(x, r);
        x.setModifiedBy(u);
        return map(roads.save(x));
    }

    /**
     * Deletes a road.
     *
     * @param u the user UUID
     * @param id the road UUID
     * @throws SmartRoadException if road not found or user not authorized
     */
    @Transactional
    public void delete(UUID u, UUID id) throws SmartRoadException {
        RoadEntity r = road(id);
        project(u, r.getProjectId());
        roads.delete(r);
    }

    /**
     * Creates a new section for a road.
     *
     * @param u the user UUID
     * @param roadId the road UUID
     * @param r the road section request DTO
     * @return the created road section response DTO
     * @throws SmartRoadException if road not found or user not authorized
     */
    @Transactional
    public RoadSectionResponseDTO createSection(UUID u, UUID roadId, RoadSectionRequestDTO r) throws SmartRoadException {
        RoadEntity road = road(roadId);
        project(u, road.getProjectId());
        RoadSectionEntity s = new RoadSectionEntity();
        s.setRoadId(roadId);
        apply(s, r);
        return sectionMap(sections.save(s));
    }

    /**
     * Retrieves all sections for a road.
     *
     * @param u the user UUID
     * @param roadId the road UUID
     * @return list of road section response DTOs
     * @throws SmartRoadException if road not found or user not authorized
     */
    @Transactional(readOnly = true)
    public List<RoadSectionResponseDTO> listSections(UUID u, UUID roadId) throws SmartRoadException {
        RoadEntity r = road(roadId);
        project(u, r.getProjectId());
        return sections.findByRoadId(roadId).stream().map(this::sectionMap).toList();
    }

    /**
     * Validates project exists and user is organization member.
     *
     * @param u the user UUID
     * @param id the project UUID
     * @return the ProjectEntity
     * @throws SmartRoadException if project not found or user not authorized
     */
    private ProjectEntity project(UUID u, UUID id) throws SmartRoadException {
        ProjectEntity p = projects.findById(id)
                .orElseThrow(() -> notFound("project.not.found"));
        orgs.requireMember(u, p.getOrganizationId());
        return p;
    }

    /**
     * Finds a road by ID.
     *
     * @param id the road UUID
     * @return the RoadEntity
     * @throws SmartRoadException if road not found
     */
    private RoadEntity road(UUID id) throws SmartRoadException {
        return roads.findById(id)
                .orElseThrow(() -> notFound("road.not.found"));
    }

    /**
     * Creates a SmartRoadException for resource not found.
     *
     * @param x the error message key
     * @return the SmartRoadException
     */
    private SmartRoadException notFound(String x) {
        return new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, x);
    }

    /**
     * Applies request data to road entity with validation.
     *
     * @param x the road entity to update
     * @param r the road request DTO
     * @throws SmartRoadException if validation fails
     */
    private void apply(RoadEntity x, RoadRequestDTO r) throws SmartRoadException {
        BigDecimal c = r.completedLengthM() == null ? BigDecimal.ZERO : r.completedLengthM();
        if (c.compareTo(r.lengthM()) > 0) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "road.completed.length.invalid");
        }
        x.setName(r.name());
        x.setLengthM(r.lengthM());
        x.setWidthM(r.widthM());
        x.setThicknessMm(r.thicknessMm());
        x.setStartChainage(r.startChainage());
        x.setEndChainage(r.endChainage());
        x.setCompletedLengthM(c);
    }

    /**
     * Applies request data to road section entity with validation.
     *
     * @param x the road section entity to update
     * @param r the road section request DTO
     * @throws SmartRoadException if validation fails
     */
    private void apply(RoadSectionEntity x, RoadSectionRequestDTO r) throws SmartRoadException {
        BigDecimal c = r.completedLengthM() == null ? BigDecimal.ZERO : r.completedLengthM();
        if (c.compareTo(r.lengthM()) > 0) {
            throw new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.SERVICE_VALIDATION_FAILED, "road.section.completed.length.invalid");
        }
        x.setStartChainage(r.startChainage());
        x.setEndChainage(r.endChainage());
        x.setLengthM(r.lengthM());
        x.setCompletedLengthM(c);
    }

    /**
     * Maps road entity to response DTO with calculations.
     *
     * @param x the road entity
     * @return the road response DTO
     */
    private RoadResponseDTO map(RoadEntity x) {
        BigDecimal rem = x.getLengthM().subtract(x.getCompletedLengthM());
        BigDecimal percentage = x.getCompletedLengthM()
                .multiply(BigDecimal.valueOf(100))
                .divide(x.getLengthM(), 2, RoundingMode.HALF_UP);
        return new RoadResponseDTO(x.getId(), x.getProjectId(), x.getName(), x.getLengthM(), x.getWidthM(),
                x.getThicknessMm(), x.getStartChainage(), x.getEndChainage(), x.getCompletedLengthM(), rem, percentage);
    }

    /**
     * Maps road section entity to response DTO with calculations.
     *
     * @param x the road section entity
     * @return the road section response DTO
     */
    private RoadSectionResponseDTO sectionMap(RoadSectionEntity x) {
        BigDecimal rem = x.getLengthM().subtract(x.getCompletedLengthM());
        BigDecimal percentage = x.getCompletedLengthM()
                .multiply(BigDecimal.valueOf(100))
                .divide(x.getLengthM(), 2, RoundingMode.HALF_UP);
        return new RoadSectionResponseDTO(x.getId(), x.getRoadId(), x.getStartChainage(), x.getEndChainage(),
                x.getLengthM(), x.getCompletedLengthM(), rem, percentage);
    }
}

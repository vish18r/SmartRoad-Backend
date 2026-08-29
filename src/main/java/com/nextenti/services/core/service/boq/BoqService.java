package com.nextenti.services.core.service.boq;

import com.nextenti.services.common.exception.ApplicationLayer;
import com.nextenti.services.common.exception.ErrorCodeMapping;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.boq.BoqItemRequest;
import com.nextenti.services.core.dto.boq.BoqItemResponse;
import com.nextenti.services.core.dto.boq.BoqRequest;
import com.nextenti.services.core.dto.boq.BoqResponse;
import com.nextenti.services.core.service.organization.OrganizationService;
import com.nextenti.services.domain.entity.BoqEntity;
import com.nextenti.services.domain.entity.BoqItemEntity;
import com.nextenti.services.domain.entity.ProjectEntity;
import com.nextenti.services.domain.repository.BoqItemRepository;
import com.nextenti.services.domain.repository.BoqRepository;
import com.nextenti.services.domain.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for BOQ (Bill of Quantities) business logic.
 * Manages BOQ creation, retrieval, and item management operations.
 */
@Service
public class BoqService {

    private final BoqRepository boqs;
    private final BoqItemRepository items;
    private final ProjectRepository projects;
    private final OrganizationService orgs;

    /**
     * Constructs a BoqService with required dependencies.
     *
     * @param b the BOQ repository
     * @param i the BOQ item repository
     * @param p the project repository
     * @param o the organization service
     */
    public BoqService(BoqRepository b, BoqItemRepository i, ProjectRepository p, OrganizationService o) {
        boqs = b;
        items = i;
        projects = p;
        orgs = o;
    }

    /**
     * Validates project exists and user is organization member.
     *
     * @param u the user UUID
     * @param id the project UUID
     * @return the ProjectEntity if valid
     * @throws SmartRoadException if project not found or user not member
     */
    private ProjectEntity project(UUID u, UUID id) throws SmartRoadException {
        ProjectEntity p = projects.findById(id)
                .orElseThrow(() -> nf("project.not.found"));
        orgs.requireMember(u, p.getOrganizationId());
        return p;
    }

    /**
     * Creates a SmartRoadException for resource not found.
     *
     * @param s the error message key
     * @return the SmartRoadException
     */
    private SmartRoadException nf(String s) {
        return new SmartRoadException(ApplicationLayer.SERVICE_LAYER, ErrorCodeMapping.DAO_NOT_FOUND, s);
    }

    /**
     * Creates a new BOQ for a project.
     *
     * @param u the user UUID
     * @param p the project UUID
     * @param r the BOQ request DTO
     * @return the created BOQ response DTO
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional
    public BoqResponse create(UUID u, UUID p, BoqRequest r) throws SmartRoadException {
        project(u, p);
        BoqEntity b = new BoqEntity();
        b.setId(UUID.randomUUID());
        b.setProjectId(p);
        b.setName(r.name());
        b.setDescription(r.description());
        b.setCreatedBy(u);
        b.setModifiedBy(u);
        return map(boqs.save(b));
    }

    /**
     * Retrieves all BOQs for a project.
     *
     * @param u the user UUID
     * @param p the project UUID
     * @return list of BOQ response DTOs
     * @throws SmartRoadException if project not found or user not authorized
     */
    @Transactional(readOnly = true)
    public List<BoqResponse> list(UUID u, UUID p) throws SmartRoadException {
        project(u, p);
        return boqs.findByProjectId(p).stream().map(this::map).toList();
    }

    /**
     * Adds an item to a BOQ.
     *
     * @param u the user UUID
     * @param bid the BOQ UUID
     * @param r the BOQ item request DTO
     * @return the created BOQ item response DTO
     * @throws SmartRoadException if BOQ not found or user not authorized
     */
    @Transactional
    public BoqItemResponse addItem(UUID u, UUID bid, BoqItemRequest r) throws SmartRoadException {
        BoqEntity b = boqs.findById(bid)
                .orElseThrow(() -> nf("boq.not.found"));
        project(u, b.getProjectId());
        BoqItemEntity i = new BoqItemEntity();
        i.setId(UUID.randomUUID());
        i.setBoqId(bid);
        i.setCreatedBy(u);
        i.setModifiedBy(u);
        apply(i, r);
        return itemMap(items.save(i));
    }

    /**
     * Retrieves all items for a BOQ.
     *
     * @param u the user UUID
     * @param bid the BOQ UUID
     * @return list of BOQ item response DTOs
     * @throws SmartRoadException if BOQ not found or user not authorized
     */
    @Transactional(readOnly = true)
    public List<BoqItemResponse> listItems(UUID u, UUID bid) throws SmartRoadException {
        BoqEntity b = boqs.findById(bid)
                .orElseThrow(() -> nf("boq.not.found"));
        project(u, b.getProjectId());
        return items.findByBoqId(bid).stream().map(this::itemMap).toList();
    }

    /**
     * Applies request data to BOQ item entity.
     *
     * @param i the BOQ item entity to update
     * @param r the BOQ item request DTO
     */
    private void apply(BoqItemEntity i, BoqItemRequest r) {
        i.setItemCode(r.itemCode());
        i.setDescription(r.description());
        i.setUnit(r.unit());
        i.setEstimatedQuantity(r.estimatedQuantity());
        i.setRate(r.rate());
        i.setActualQuantity(r.actualQuantity() == null ? BigDecimal.ZERO : r.actualQuantity());
        i.setActualRate(r.actualRate() == null ? r.rate() : r.actualRate());
    }

    /**
     * Maps BOQ entity to response DTO.
     *
     * @param b the BOQ entity
     * @return the BOQ response DTO
     */
    private BoqResponse map(BoqEntity b) {
        return new BoqResponse(b.getId(), b.getProjectId(), b.getName(), b.getDescription());
    }

    /**
     * Maps BOQ item entity to response DTO.
     *
     * @param i the BOQ item entity
     * @return the BOQ item response DTO
     */
    private BoqItemResponse itemMap(BoqItemEntity i) {
        BigDecimal est = i.getEstimatedQuantity().multiply(i.getRate());
        BigDecimal act = i.getActualQuantity().multiply(i.getActualRate());
        return new BoqItemResponse(i.getId(), i.getBoqId(), i.getItemCode(), i.getDescription(), i.getUnit(),
                i.getEstimatedQuantity(), i.getRate(), est, i.getActualQuantity(), i.getActualRate(), act,
                i.getActualQuantity().subtract(i.getEstimatedQuantity()), act.subtract(est));
    }
}

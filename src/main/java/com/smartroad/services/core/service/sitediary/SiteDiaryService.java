package com.smartroad.services.core.service.sitediary;

import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.sitediary.SiteDiaryRequestDTO;
import com.smartroad.services.core.dto.sitediary.SiteDiaryResponseDTO;
import com.smartroad.services.domain.entity.sitediary.SiteDiaryEntity;
import com.smartroad.services.domain.repository.SiteDiaryRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for daily site diary business logic.
 * Coordinates between {@link SiteDiaryRepository} and {@link ProjectRepository}.
 */
@Service
public class SiteDiaryService {

    private static final Logger logger = LoggerFactory.getLogger(SiteDiaryService.class);

    private final SiteDiaryRepository siteDiaryRepository;
    private final ProjectRepository projectRepository;

    /**
     * Constructs the service with required repository dependencies.
     *
     * @param siteDiaryRepository the site diary repository
     * @param projectRepository the project repository
     */
    public SiteDiaryService(SiteDiaryRepository siteDiaryRepository, ProjectRepository projectRepository) {
        this.siteDiaryRepository = siteDiaryRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Retrieves the diary entry for a specific project and date.
     *
     * @param projectId the project UUID
     * @param date the diary date
     * @return the {@link SiteDiaryResponseDTO} for the found entry
     * @throws SmartRoadException if no entry exists for that project and date
     */
    public SiteDiaryResponseDTO getDiaryEntry(UUID projectId, LocalDate date) throws SmartRoadException {
        SiteDiaryEntity entity = siteDiaryRepository.findByProjectIdAndDiaryDate(projectId, date)
                .orElseThrow(() -> new SmartRoadException(
                        ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND,
                        "Site diary entry not found for project " + projectId + " on " + date
                ));
        return toResponseDTO(entity);
    }

    /**
     * Retrieves all diary entries for a project, most-recent first.
     *
     * @param projectId the project UUID
     * @return list of {@link SiteDiaryResponseDTO}
     * @throws SmartRoadException if the project does not exist
     */
    public List<SiteDiaryResponseDTO> getDiaryEntries(UUID projectId) throws SmartRoadException {
        validateProjectExists(projectId);
        return siteDiaryRepository.findByProjectIdOrderByDiaryDateDesc(projectId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates or updates the diary entry for a project on a given date (upsert).
     * If an entry already exists for that project+date, it is updated in place.
     *
     * @param dto the request payload
     * @return the saved {@link SiteDiaryResponseDTO}
     * @throws SmartRoadException if the project does not exist
     */
    @Transactional
    public SiteDiaryResponseDTO saveDiaryEntry(SiteDiaryRequestDTO dto) throws SmartRoadException {
        validateProjectExists(dto.projectId());

        SiteDiaryEntity entity = siteDiaryRepository
                .findByProjectIdAndDiaryDate(dto.projectId(), dto.diaryDate())
                .orElseGet(SiteDiaryEntity::new);

        entity.setProjectId(dto.projectId());
        entity.setDiaryDate(dto.diaryDate());
        entity.setWeather(dto.weather());
        entity.setTemperatureCelsius(dto.temperatureCelsius());
        entity.setSiteConditions(dto.siteConditions());
        entity.setWorkSummary(dto.workSummary());
        entity.setIssues(dto.issues());
        entity.setSafetyNotes(dto.safetyNotes());
        entity.setNotes(dto.notes());

        SiteDiaryEntity saved = siteDiaryRepository.save(entity);
        logger.info("Site diary entry saved for project {} on {}", dto.projectId(), dto.diaryDate());
        return toResponseDTO(saved);
    }

    /**
     * Deletes a site diary entry by its ID.
     *
     * @param id the diary entry UUID
     * @throws SmartRoadException if the entry is not found
     */
    @Transactional
    public void deleteDiaryEntry(UUID id) throws SmartRoadException {
        if (!siteDiaryRepository.existsById(id)) {
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "Site diary entry not found: " + id
            );
        }
        siteDiaryRepository.deleteById(id);
    }

    /**
     * Validates that the project exists.
     *
     * @param projectId the project UUID to validate
     * @throws SmartRoadException if the project does not exist
     */
    private void validateProjectExists(UUID projectId) throws SmartRoadException {
        if (!projectRepository.existsById(projectId)) {
            throw new SmartRoadException(
                    ApplicationLayer.SERVICE_LAYER,
                    ErrorCodeMapping.DAO_NOT_FOUND,
                    "Project not found: " + projectId
            );
        }
    }

    /**
     * Converts a {@link SiteDiaryEntity} to a {@link SiteDiaryResponseDTO}.
     *
     * @param entity the source entity
     * @return the mapped response DTO
     */
    private SiteDiaryResponseDTO toResponseDTO(SiteDiaryEntity entity) {
        return new SiteDiaryResponseDTO(
                entity.getId(),
                entity.getProjectId(),
                entity.getDiaryDate(),
                entity.getWeather(),
                entity.getTemperatureCelsius(),
                entity.getSiteConditions(),
                entity.getWorkSummary(),
                entity.getIssues(),
                entity.getSafetyNotes(),
                entity.getNotes(),
                entity.getDateCreated(),
                entity.getDateModified()
        );
    }
}

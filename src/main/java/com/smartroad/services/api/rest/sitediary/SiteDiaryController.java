package com.smartroad.services.api.rest.sitediary;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.sitediary.SiteDiaryRequestDTO;
import com.smartroad.services.core.dto.sitediary.SiteDiaryResponseDTO;
import com.smartroad.services.core.service.sitediary.SiteDiaryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for daily site diary operations.
 * Handles HTTP requests and delegates all business logic to {@link SiteDiaryService}.
 */
@RestController
@RequestMapping("/api/v1/site-diary")
public class SiteDiaryController {

    private static final Logger logger = LoggerFactory.getLogger(SiteDiaryController.class);

    private final SiteDiaryService siteDiaryService;

    /**
     * Constructs the controller with the required service dependency.
     *
     * @param siteDiaryService the site diary service
     */
    public SiteDiaryController(SiteDiaryService siteDiaryService) {
        this.siteDiaryService = siteDiaryService;
    }

    /**
     * Retrieves all diary entries for a project, most-recent first.
     *
     * @param projectId the UUID of the project
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing list of {@link SiteDiaryResponseDTO}
     * @throws SmartRoadException if the project is not found
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDiaryEntries(@RequestParam UUID projectId,
                                                   @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getDiaryEntries method--");
        List<SiteDiaryResponseDTO> response = siteDiaryService.getDiaryEntries(projectId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves the diary entry for a specific project and date.
     *
     * @param projectId the UUID of the project
     * @param date the diary date in ISO format (yyyy-MM-dd)
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing {@link SiteDiaryResponseDTO}
     * @throws SmartRoadException if the entry is not found
     */
    @GetMapping(path = "/entry", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDiaryEntry(@RequestParam UUID projectId,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getDiaryEntry method--");
        SiteDiaryResponseDTO response = siteDiaryService.getDiaryEntry(projectId, date);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Creates or updates (upsert) the diary entry for a project on a given date.
     *
     * @param dto the diary entry payload
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the saved {@link SiteDiaryResponseDTO}
     * @throws SmartRoadException if the project is not found
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> saveDiaryEntry(@RequestBody @Valid SiteDiaryRequestDTO dto,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside saveDiaryEntry method--");
        SiteDiaryResponseDTO response = siteDiaryService.saveDiaryEntry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Deletes a site diary entry by its ID.
     *
     * @param id the UUID of the diary entry
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with 204 No Content
     * @throws SmartRoadException if the entry is not found
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteDiaryEntry(@PathVariable UUID id,
                                                    @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteDiaryEntry method--");
        siteDiaryService.deleteDiaryEntry(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

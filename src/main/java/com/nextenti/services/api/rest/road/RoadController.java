package com.nextenti.services.api.rest.road;

import com.nextenti.services.common.exception.NextentiException;
import com.nextenti.services.core.dto.road.RoadRequest;
import com.nextenti.services.core.dto.road.RoadResponse;
import com.nextenti.services.core.dto.road.RoadSectionRequest;
import com.nextenti.services.core.dto.road.RoadSectionResponse;
import com.nextenti.services.core.service.road.RoadService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for RoadEntity management.
 * Handles HTTP requests for road and road section CRUD operations, delegating all business logic to {@link RoadService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
public class RoadController {

    private static final Logger logger = LoggerFactory.getLogger(RoadController.class);

    private final RoadService roadService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param roadService the road service
     */
    public RoadController(RoadService roadService) {
        this.roadService = roadService;
    }

    /**
     * Creates a new road for a project.
     *
     * @param projectId the UUID of the project
     * @param request the road creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link RoadResponse}
     * @throws NextentiException if project not found or creation fails
     */
    @PostMapping(path = "/projects/{projectId}/roads", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createRoad(@PathVariable UUID projectId,
                                             @RequestBody @Valid RoadRequest request,
                                             @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside createRoadEntity method--");

        RoadResponse response = roadService.create(projectId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all roads for a project.
     *
     * @param projectId the UUID of the project
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link RoadResponse}
     * @throws NextentiException if project not found or retrieval fails
     */
    @GetMapping(path = "/projects/{projectId}/roads", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listRoads(@PathVariable UUID projectId,
                                            @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside listRoads method--");

        List<RoadResponse> response = roadService.list(projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a road by ID.
     *
     * @param id the UUID of the road
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link RoadResponse}
     * @throws NextentiException if road not found
     */
    @GetMapping(path = "/roads/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getRoad(@PathVariable UUID id,
                                          @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside getRoadEntity method--");

        RoadResponse response = roadService.get(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a road.
     *
     * @param id the UUID of the road
     * @param request the road update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link RoadResponse}
     * @throws NextentiException if road not found or update fails
     */
    @PutMapping(path = "/roads/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateRoad(@PathVariable UUID id,
                                             @RequestBody @Valid RoadRequest request,
                                             @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside updateRoadEntity method--");

        RoadResponse response = roadService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a road.
     *
     * @param id the UUID of the road
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws NextentiException if road not found or deletion fails
     */
    @DeleteMapping(path = "/roads/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteRoad(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside deleteRoadEntity method--");

        roadService.delete(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Creates a new road section.
     *
     * @param roadId the UUID of the road
     * @param request the road section creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link RoadSectionResponse}
     * @throws NextentiException if road not found or creation fails
     */
    @PostMapping(path = "/roads/{roadId}/sections", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createRoadSection(@PathVariable UUID roadId,
                                                    @RequestBody @Valid RoadSectionRequest request,
                                                    @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside createRoadSectionEntity method--");

        RoadSectionResponse response = roadService.createSection(roadId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all road sections for a road.
     *
     * @param roadId the UUID of the road
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link RoadSectionResponse}
     * @throws NextentiException if road not found or retrieval fails
     */
    @GetMapping(path = "/roads/{roadId}/sections", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listRoadSections(@PathVariable UUID roadId,
                                                   @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside listRoadSections method--");

        List<RoadSectionResponse> response = roadService.listSections(roadId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

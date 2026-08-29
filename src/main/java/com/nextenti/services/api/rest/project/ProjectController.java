package com.nextenti.services.api.rest.project;

import com.nextenti.services.common.exception.NextentiException;
import com.nextenti.services.core.dto.project.ProjectRequest;
import com.nextenti.services.core.dto.project.ProjectResponse;
import com.nextenti.services.core.service.project.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for ProjectEntity management.
 * Handles HTTP requests for project CRUD operations, delegating all business logic to {@link ProjectService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param projectService the project service
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Creates a new project.
     *
     * @param request the project creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ProjectResponse}
     * @throws NextentiException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createProject(@RequestBody @Valid ProjectRequest request,
                                                @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside createProjectEntity method--");

        ProjectResponse response = projectService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all projects for an organization.
     *
     * @param organizationId the UUID of the organization
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ProjectResponse}
     * @throws NextentiException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listProjects(@RequestParam UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside listProjects method--");

        List<ProjectResponse> response = projectService.list(organizationId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a project by ID.
     *
     * @param id the UUID of the project
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ProjectResponse}
     * @throws NextentiException if project not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getProject(@PathVariable UUID id,
                                             @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside getProjectEntity method--");

        ProjectResponse response = projectService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a project.
     *
     * @param id the UUID of the project
     * @param request the project update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ProjectResponse}
     * @throws NextentiException if project not found or update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateProject(@PathVariable UUID id,
                                                @RequestBody @Valid ProjectRequest request,
                                                @RequestHeader HttpHeaders headers) throws NextentiException {
        logger.info("--Inside updateProjectEntity method--");

        ProjectResponse response = projectService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

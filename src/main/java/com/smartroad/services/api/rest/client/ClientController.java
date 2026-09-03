package com.smartroad.services.api.rest.client;

import com.smartroad.services.api.utils.RequestUtil;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.client.ClientRequestDTO;
import com.smartroad.services.core.dto.client.ClientResponseDTO;
import com.smartroad.services.core.service.client.ClientService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for ClientEntity management.
 * Handles HTTP requests for client CRUD operations, delegating all business logic to {@link ClientService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param clientService the client service
     */
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Creates a new client.
     *
     * @param request the client creation request
     * @param organizationId the organization ID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ClientResponseDTO}
     * @throws NextentiException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createClient(@RequestBody @Valid ClientRequestDTO request,
                                               @RequestParam UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createClientEntity method--");

        UUID userId = RequestUtil.extractUserId();
        ClientResponseDTO response = clientService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all clients for an organization.
     *
     * @param organizationId the organization ID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ClientResponseDTO}
     * @throws NextentiException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listClients(@RequestParam UUID organizationId,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listClients method--");

        UUID userId = RequestUtil.extractUserId();
        List<ClientResponseDTO> response = clientService.list(userId, organizationId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a client by ID.
     *
     * @param id the UUID of the client
     * @param organizationId the organization ID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ClientResponseDTO}
     * @throws NextentiException if client not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getClient(@PathVariable UUID id,
                                            @RequestParam UUID organizationId,
                                            @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getClientEntity method--");

        UUID userId = RequestUtil.extractUserId();
        ClientResponseDTO response = clientService.get(userId, id, organizationId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a client.
     *
     * @param id the UUID of the client
     * @param request the client update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ClientResponseDTO}
     * @throws NextentiException if client not found or update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateClient(@PathVariable UUID id,
                                               @RequestBody @Valid ClientRequestDTO request,
                                               @RequestParam UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateClientEntity method--");

        UUID userId = RequestUtil.extractUserId();
        ClientResponseDTO response = clientService.update(userId, id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a client.
     *
     * @param id the UUID of the client
     * @param organizationId the organization ID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws NextentiException if client not found or deletion fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteClient(@PathVariable UUID id,
                                               @RequestParam UUID organizationId,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteClientEntity method--");

        UUID userId = RequestUtil.extractUserId();
        clientService.delete(userId, id, organizationId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

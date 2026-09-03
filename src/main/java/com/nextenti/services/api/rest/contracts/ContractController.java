package com.nextenti.services.api.rest.contracts;

import com.nextenti.services.api.utils.RequestUtil;
import com.nextenti.services.common.exception.SmartRoadException;
import com.nextenti.services.core.dto.contracts.ContractRequestDTO;
import com.nextenti.services.core.dto.contracts.ContractResponseDTO;
import com.nextenti.services.core.service.contracts.ContractService;
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
 * REST controller for contract management.
 * Handles HTTP requests for contract CRUD operations, delegating all business logic to {@link ContractService}.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private static final Logger logger = LoggerFactory.getLogger(ContractController.class);

    private final ContractService contractService;

    /**
     * Constructs the controller with required service dependency.
     *
     * @param contractService the contract service
     */
    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    /**
     * Creates a new contract.
     *
     * @param request the contract creation request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the created {@link ContractResponseDTO}
     * @throws SmartRoadException if creation fails
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createContract(@RequestBody @Valid ContractRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createContract method--");

        UUID userId = RequestUtil.extractUserId();
        ContractResponseDTO response = contractService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all contracts for a project.
     *
     * @param projectId the project UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ContractResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listContracts(@RequestParam UUID projectId,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listContracts method--");

        UUID userId = RequestUtil.extractUserId();
        List<ContractResponseDTO> response = contractService.listByProject(projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a contract by ID.
     *
     * @param id the contract UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the {@link ContractResponseDTO}
     * @throws SmartRoadException if contract not found
     */
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getContract(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getContract method--");

        UUID userId = RequestUtil.extractUserId();
        ContractResponseDTO response = contractService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Updates a contract.
     *
     * @param id the contract UUID
     * @param request the contract update request
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing the updated {@link ContractResponseDTO}
     * @throws SmartRoadException if update fails
     */
    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateContract(@PathVariable UUID id,
                                                 @RequestBody @Valid ContractRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateContract method--");

        UUID userId = RequestUtil.extractUserId();
        ContractResponseDTO response = contractService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a contract.
     *
     * @param id the contract UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with no content
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteContract(@PathVariable UUID id,
                                               @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteContract method--");

        UUID userId = RequestUtil.extractUserId();
        contractService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

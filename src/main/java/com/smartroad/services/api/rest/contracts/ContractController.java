package com.smartroad.services.api.rest.contracts;

import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.common.util.NextentiConstants;
import com.smartroad.services.core.dto.contracts.ContractRequestDTO;
import com.smartroad.services.core.dto.contracts.ContractResponseDTO;
import com.smartroad.services.core.service.contracts.ContractService;
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
 * Mapped at the API root so contracts can also be addressed as a nested resource of a project or a client.
 *
 * @author Vishal
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
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
    @PostMapping(path = "/contracts", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createContract(@RequestBody @Valid ContractRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside createContract method--");

        ContractResponseDTO response = contractService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves contracts, optionally narrowed to a single project.
     * The project filter is accepted either as the projectId query parameter or as the
     * x-project-id header the web client sends; when neither is present every contract is returned.
     *
     * @param projectId the optional project UUID supplied as a query parameter
     * @param projectIdHeader the optional project UUID supplied as the x-project-id header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ContractResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listContracts(@RequestParam(required = false) UUID projectId,
                                                @RequestHeader(name = NextentiConstants.HEADER_PROJECT_ID, required = false) UUID projectIdHeader,
                                                @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listContracts method--");

        UUID filter = projectId != null ? projectId : projectIdHeader;
        List<ContractResponseDTO> response = filter == null
                ? contractService.listAll()
                : contractService.listByProject(filter);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Searches contracts by contract number, work order number, or agreement number.
     * The term is read from the x-search header the web client sends, falling back to the
     * query request parameter.
     *
     * @param query the optional search term supplied as a query parameter
     * @param searchHeader the optional search term supplied as the x-search header
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of matching {@link ContractResponseDTO}
     * @throws SmartRoadException if the search fails
     */
    @GetMapping(path = "/contracts/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> searchContracts(@RequestParam(required = false) String query,
                                                  @RequestHeader(name = NextentiConstants.HEADER_SEARCH, required = false) String searchHeader,
                                                  @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside searchContracts method--");

        String term = query != null ? query : searchHeader;
        List<ContractResponseDTO> response = contractService.search(term);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves all contracts belonging to a project.
     *
     * @param projectId the project UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ContractResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/projects/{projectId}/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listContractsByProject(@PathVariable UUID projectId,
                                                         @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listContractsByProject method--");

        List<ContractResponseDTO> response = contractService.listByProject(projectId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves all contracts raised for a client.
     *
     * @param clientId the client UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} containing a list of {@link ContractResponseDTO}
     * @throws SmartRoadException if retrieval fails
     */
    @GetMapping(path = "/clients/{clientId}/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> listContractsByClient(@PathVariable UUID clientId,
                                                        @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside listContractsByClient method--");

        List<ContractResponseDTO> response = contractService.listByClient(clientId);

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
    @GetMapping(path = "/contracts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getContract(@PathVariable UUID id,
                                              @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside getContract method--");

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
    @PutMapping(path = "/contracts/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateContract(@PathVariable UUID id,
                                                 @RequestBody @Valid ContractRequestDTO request,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside updateContract method--");

        ContractResponseDTO response = contractService.update(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deletes a contract.
     *
     * @param id the contract UUID
     * @param headers the HTTP request headers
     * @return {@link ResponseEntity} with HTTP 200 OK on successful deletion
     * @throws SmartRoadException if delete fails
     */
    @DeleteMapping(path = "/contracts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteContract(@PathVariable UUID id,
                                                 @RequestHeader HttpHeaders headers) throws SmartRoadException {
        logger.info("--Inside deleteContract method--");

        contractService.delete(id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

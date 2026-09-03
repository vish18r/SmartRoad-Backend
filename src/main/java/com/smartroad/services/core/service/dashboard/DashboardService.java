package com.smartroad.services.core.service.dashboard;

import com.smartroad.services.core.dto.dashboard.DashboardStatsDTO;
import com.smartroad.services.core.service.workers.WorkerService;
import com.smartroad.services.domain.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service layer for dashboard statistics and metrics.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final WorkerService workerService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param projectRepository the project repository
     * @param workerService the worker service
     */
    public DashboardService(ProjectRepository projectRepository, WorkerService workerService) {
        this.projectRepository = projectRepository;
        this.workerService = workerService;
    }

    /**
     * Retrieves dashboard statistics for an organization.
     *
     * @param organizationId the UUID of the organization
     * @return {@link DashboardStatsDTO} containing statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(UUID organizationId) {
        long activeProjects = projectRepository.countActiveProjectsByOrganization(organizationId);
        long completedProjects = projectRepository.countCompletedProjectsByOrganization(organizationId);
        long activeWorkers = workerService.getWorkerCountByOrganization(organizationId);

        return new DashboardStatsDTO(
            activeProjects,
            completedProjects,
            activeWorkers,
            0.0,
            "operational"
        );
    }
}

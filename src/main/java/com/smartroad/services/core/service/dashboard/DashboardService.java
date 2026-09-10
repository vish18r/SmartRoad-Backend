package com.smartroad.services.core.service.dashboard;

import com.smartroad.services.common.enums.workers.WorkerStatusEnum;
import com.smartroad.services.core.dto.dashboard.DashboardActivityDTO;
import com.smartroad.services.core.dto.dashboard.DashboardProjectDTO;
import com.smartroad.services.core.dto.dashboard.DashboardResponseDTO;
import com.smartroad.services.core.dto.dashboard.DashboardStatsDTO;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.RoadRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for dashboard statistics and metrics.
 * Aggregates project, road, and workforce counters for a single organization.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class DashboardService {

    private static final int RECENT_PROJECT_LIMIT = 10;

    private final ProjectRepository projectRepository;
    private final RoadRepository roadRepository;
    private final WorkerRepository workerRepository;

    /**
     * Constructs the service with required dependencies.
     *
     * @param projectRepository the project repository
     * @param roadRepository the road repository
     * @param workerRepository the worker repository
     */
    public DashboardService(ProjectRepository projectRepository,
                            RoadRepository roadRepository,
                            WorkerRepository workerRepository) {
        this.projectRepository = projectRepository;
        this.roadRepository = roadRepository;
        this.workerRepository = workerRepository;
    }

    /**
     * Retrieves dashboard statistics for an organization.
     *
     * @param organizationId the UUID of the organization
     * @return {@link DashboardStatsDTO} containing statistics
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(UUID organizationId) {
        return new DashboardStatsDTO(
            projectRepository.countProjectsByOrganization(organizationId),
            projectRepository.countActiveProjectsByOrganization(organizationId),
            projectRepository.countCompletedProjectsByOrganization(organizationId),
            projectRepository.countOnHoldProjectsByOrganization(organizationId),
            orZero(projectRepository.sumBudgetByOrganization(organizationId)),
            orZero(projectRepository.sumActualCostByOrganization(organizationId)),
            roadRepository.countByOrganization(organizationId),
            roadRepository.countCompletedByOrganization(organizationId),
            workerRepository.countByOrganizationId(organizationId),
            workerRepository.countByOrganizationIdAndStatus(organizationId, WorkerStatusEnum.ACTIVE),
            orZero(projectRepository.sumActualCostByOrganization(organizationId)).doubleValue(),
            "operational",
            getProjectBreakdown(organizationId)
        );
    }

    /**
     * Builds the aggregate dashboard payload: the organization's statistics plus a recent activity feed.
     * The activity feed is derived from the organization's most recently modified projects.
     *
     * @param organizationId the UUID of the organization
     * @return {@link DashboardResponseDTO} containing statistics and recent activity
     */
    @Transactional(readOnly = true)
    public DashboardResponseDTO getDashboard(UUID organizationId) {
        return new DashboardResponseDTO(getDashboardStats(organizationId), getRecentActivities(organizationId));
    }

    /**
     * Builds the per-project progress breakdown shown by the dashboard's project widget.
     *
     * @param organizationId the UUID of the organization
     * @return list of {@link DashboardProjectDTO} ordered by most recently modified first
     */
    @Transactional(readOnly = true)
    public List<DashboardProjectDTO> getProjectBreakdown(UUID organizationId) {
        return recentProjects(organizationId).stream()
            .map(project -> new DashboardProjectDTO(project.getId(), project.getName(),
                    orZero(project.getProgress()), project.getStatus()))
            .toList();
    }

    /**
     * Derives a recent activity feed from the organization's most recently modified projects.
     *
     * @param organizationId the UUID of the organization
     * @return list of {@link DashboardActivityDTO} ordered by most recently modified first
     */
    private List<DashboardActivityDTO> getRecentActivities(UUID organizationId) {
        return recentProjects(organizationId).stream()
            .map(project -> new DashboardActivityDTO(project.getId(), "PROJECT_UPDATED",
                    project.getName() + " is " + project.getStatus(), project.getDateModified()))
            .toList();
    }

    /**
     * Loads the organization's non-archived projects, most recently modified first, capped to a
     * dashboard-sized page.
     *
     * @param organizationId the UUID of the organization
     * @return list of recently touched {@link ProjectEntity} records
     */
    private List<ProjectEntity> recentProjects(UUID organizationId) {
        return projectRepository.findByOrganizationIdAndArchivedFalse(organizationId).stream()
            .sorted(Comparator.comparing(ProjectEntity::getDateModified,
                    Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(RECENT_PROJECT_LIMIT)
            .toList();
    }

    /**
     * Substitutes zero for a null aggregate, so the dashboard never returns null totals.
     *
     * @param value the aggregate value, possibly null
     * @return the value, or {@link BigDecimal#ZERO} when null
     */
    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

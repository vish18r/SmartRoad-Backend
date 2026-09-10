package com.smartroad.services.core.service.dashboard;

import com.smartroad.services.common.enums.workers.AttendanceStatusEnum;
import com.smartroad.services.common.exception.ApplicationLayer;
import com.smartroad.services.common.exception.ErrorCodeMapping;
import com.smartroad.services.common.exception.SmartRoadException;
import com.smartroad.services.core.dto.dashboard.SiteDashboardResponseDTO;
import com.smartroad.services.core.dto.dashboard.SiteMaterialsDTO;
import com.smartroad.services.core.dto.dashboard.SitePhotoDTO;
import com.smartroad.services.core.dto.dashboard.SitePhotosDTO;
import com.smartroad.services.core.dto.dashboard.SiteWorkDTO;
import com.smartroad.services.core.dto.dashboard.SiteWorkforceDTO;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.project.ProjectEntity;
import com.smartroad.services.domain.entity.tracking.WorkPhotoEntity;
import com.smartroad.services.domain.entity.tracking.WorkerCheckinEntity;
import com.smartroad.services.domain.repository.MaterialStockRepository;
import com.smartroad.services.domain.repository.ProjectRepository;
import com.smartroad.services.domain.repository.RoadRepository;
import com.smartroad.services.domain.repository.WorkerAttendanceRepository;
import com.smartroad.services.domain.repository.WorkerRepository;
import com.smartroad.services.domain.repository.tracking.WorkPhotoRepository;
import com.smartroad.services.domain.repository.tracking.WorkerCheckinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for the supervisor's site dashboard.
 * Assembles one site's daily picture from the project, road, workforce, material
 * stock, and site-photo records that already exist for that project.
 *
 * @author Vishal
 * @version 1.0
 */
@Service
public class SiteDashboardService {

    private static final int RECENT_PHOTO_LIMIT = 12;

    private final ProjectRepository projectRepository;
    private final RoadRepository roadRepository;
    private final WorkerRepository workerRepository;
    private final WorkerAttendanceRepository attendanceRepository;
    private final MaterialStockRepository stockRepository;
    private final WorkPhotoRepository photoRepository;
    private final WorkerCheckinRepository checkinRepository;
    private final OrganizationService organizationService;

    /**
     * Constructs the service with required dependencies.
     *
     * @param projectRepository the project repository
     * @param roadRepository the road repository
     * @param workerRepository the worker repository
     * @param attendanceRepository the worker attendance repository
     * @param stockRepository the per-project material stock repository
     * @param photoRepository the site photo repository
     * @param checkinRepository the worker check-in repository
     * @param organizationService the organization service, used to authorise the caller
     */
    public SiteDashboardService(ProjectRepository projectRepository,
                                RoadRepository roadRepository,
                                WorkerRepository workerRepository,
                                WorkerAttendanceRepository attendanceRepository,
                                MaterialStockRepository stockRepository,
                                WorkPhotoRepository photoRepository,
                                WorkerCheckinRepository checkinRepository,
                                OrganizationService organizationService) {
        this.projectRepository = projectRepository;
        this.roadRepository = roadRepository;
        this.workerRepository = workerRepository;
        this.attendanceRepository = attendanceRepository;
        this.stockRepository = stockRepository;
        this.photoRepository = photoRepository;
        this.checkinRepository = checkinRepository;
        this.organizationService = organizationService;
    }

    /**
     * Builds the site dashboard for one project on one day.
     *
     * @param userId the UUID of the requesting user
     * @param projectId the project (site) UUID
     * @param date the day to report on
     * @return the assembled {@link SiteDashboardResponseDTO}
     * @throws SmartRoadException if the project is not found or the user is not a
     *                            member of the owning organization
     */
    @Transactional(readOnly = true)
    public SiteDashboardResponseDTO getSiteDashboard(UUID userId, UUID projectId, LocalDate date) throws SmartRoadException {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new SmartRoadException(ApplicationLayer.SERVICE_LAYER,
                        ErrorCodeMapping.DAO_NOT_FOUND, "project.not.found"));
        organizationService.requireMember(userId, project.getOrganizationId());

        return new SiteDashboardResponseDTO(
                project.getId(),
                project.getName(),
                project.getCode(),
                date,
                buildWork(project),
                buildWorkforce(projectId, date),
                buildMaterials(projectId),
                buildPhotos(projectId, date));
    }

    /**
     * Rolls up the road lengths on the site into a single progress figure.
     *
     * @param project the project being reported on
     * @return the {@link SiteWorkDTO} for the site
     */
    private SiteWorkDTO buildWork(ProjectEntity project) {
        UUID projectId = project.getId();
        BigDecimal totalLength = orZero(roadRepository.sumLengthByProjectId(projectId));
        BigDecimal completedLength = orZero(roadRepository.sumCompletedLengthByProjectId(projectId));

        return new SiteWorkDTO(
                project.getStatus(),
                orZero(project.getProgress()),
                roadRepository.countByProjectId(projectId),
                roadRepository.countCompletedByProjectId(projectId),
                totalLength,
                completedLength,
                totalLength.subtract(completedLength).max(BigDecimal.ZERO),
                percentage(completedLength, totalLength));
    }

    /**
     * Combines the attendance register with live GPS check-ins for the day.
     *
     * @param projectId the project UUID
     * @param date the day to report on
     * @return the {@link SiteWorkforceDTO} for the site
     */
    private SiteWorkforceDTO buildWorkforce(UUID projectId, LocalDate date) {
        long present = countAttendance(projectId, date, AttendanceStatusEnum.PRESENT);
        long absent = countAttendance(projectId, date, AttendanceStatusEnum.ABSENT);
        long halfDay = countAttendance(projectId, date, AttendanceStatusEnum.HALF_DAY);
        long onLeave = countAttendance(projectId, date, AttendanceStatusEnum.LEAVE);

        List<WorkerCheckinEntity> checkins = checkinRepository.findByProjectAndDate(projectId, date);
        long checkedOut = checkins.stream().filter(checkin -> checkin.getCheckOutTime() != null).count();

        return new SiteWorkforceDTO(
                workerRepository.countByAssignedSiteId(projectId),
                present,
                absent,
                halfDay,
                onLeave,
                present + absent + halfDay + onLeave,
                checkins.size(),
                checkedOut,
                checkins.size() - checkedOut,
                orZero(attendanceRepository.sumHoursByProjectAndDate(projectId, date)));
    }

    /**
     * Counts the site's attendance records for the day in a single status.
     *
     * @param projectId the project UUID
     * @param date the day to report on
     * @param status the attendance status to count
     * @return count of matching records
     */
    private long countAttendance(UUID projectId, LocalDate date, AttendanceStatusEnum status) {
        return attendanceRepository.countByProjectAndDateAndStatus(projectId, date, status);
    }

    /**
     * Summarises the material stock the site is currently holding.
     *
     * @param projectId the project UUID
     * @return the {@link SiteMaterialsDTO} for the site
     */
    private SiteMaterialsDTO buildMaterials(UUID projectId) {
        return new SiteMaterialsDTO(
                stockRepository.countByProjectId(projectId),
                stockRepository.countLowStockByProjectId(projectId),
                orZero(stockRepository.sumStockValueByProjectId(projectId)));
    }

    /**
     * Collects the photos captured on the site that day, newest first.
     *
     * @param projectId the project UUID
     * @param date the day to report on
     * @return the {@link SitePhotosDTO} for the site
     */
    private SitePhotosDTO buildPhotos(UUID projectId, LocalDate date) {
        List<WorkPhotoEntity> photos = photoRepository.findByProjectAndDate(projectId, date);
        List<SitePhotoDTO> recent = photos.stream()
                .limit(RECENT_PHOTO_LIMIT)
                .map(this::toPhoto)
                .toList();
        return new SitePhotosDTO(photos.size(), recent);
    }

    /**
     * Maps a stored site photo to its dashboard projection.
     *
     * @param photo the work photo entity
     * @return the site photo DTO
     */
    private SitePhotoDTO toPhoto(WorkPhotoEntity photo) {
        return new SitePhotoDTO(photo.getId(), photo.getPhotoType(), photo.getPhotoUrl(),
                photo.getPhotoDate(), photo.getWorkArea(), photo.getDescription());
    }

    /**
     * Expresses completed length as a percentage of planned length.
     *
     * @param completed the completed length
     * @param total the planned length
     * @return the percentage, or zero when nothing is planned
     */
    private BigDecimal percentage(BigDecimal completed, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return completed.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
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

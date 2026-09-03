package com.nextenti.services.domain.repository.tracking;

import com.nextenti.services.domain.entity.tracking.WorkCompletionCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for WorkCompletionCertificateEntity.
 */
@Repository
public interface WorkCompletionCertificateRepository extends JpaRepository<WorkCompletionCertificateEntity, UUID> {

    List<WorkCompletionCertificateEntity> findByProjectIdOrderByCompletionDateDesc(UUID projectId);

    List<WorkCompletionCertificateEntity> findByProjectIdAndStatus(UUID projectId, String status);
}

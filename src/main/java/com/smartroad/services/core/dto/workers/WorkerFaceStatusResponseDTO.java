package com.smartroad.services.core.dto.workers;

import com.smartroad.services.common.enums.workers.WorkerFaceStatusEnum;
import java.util.UUID;

/**
 * A worker's face registration status. Carries no biometric data.
 *
 * @param workerId the worker UUID
 * @param employeeId the worker's employee ID, e.g. SR1
 * @param faceStatus whether the worker has a registered face
 */
public record WorkerFaceStatusResponseDTO(
    UUID workerId,
    String employeeId,
    WorkerFaceStatusEnum faceStatus
) {}

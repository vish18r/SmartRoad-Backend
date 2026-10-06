package com.smartroad.services.core.dto.workers;

/**
 * Request body for registering or replacing a worker's face template.
 *
 * @param faceDescriptor the 128-value face embedding as a JSON array of numbers
 */
public record WorkerFaceRequestDTO(
    String faceDescriptor
) {}

package com.smartroad.services.core.mapper.shift;

import com.smartroad.services.core.dto.shift.ShiftAssignmentResponseDTO;
import com.smartroad.services.domain.entity.shift.ShiftTemplateEntity;
import com.smartroad.services.domain.entity.shift.WorkerShiftAssignmentEntity;
import com.smartroad.services.domain.entity.worker.WorkerEntity;
import org.springframework.stereotype.Component;

/**
 * Assembles {@link ShiftAssignmentResponseDTO} from a persisted assignment together with the
 * worker and shift template it references. Composed by hand rather than by MapStruct because the
 * response draws on three entities that are never related by a single JPA association, the same
 * reasoning {@code PayrollMapper} uses for payroll responses.
 *
 * @author Vishal
 * @version 1.0
 */
@Component
public class ShiftAssignmentMapper {

    /**
     * Builds the response DTO for a persisted assignment.
     *
     * @param entity the persisted assignment entity
     * @param worker the assigned worker, or null when unknown
     * @param shift the assigned shift template, or null when unknown
     * @return the assembled {@link ShiftAssignmentResponseDTO}
     */
    public ShiftAssignmentResponseDTO toShiftAssignmentResponseDTO(WorkerShiftAssignmentEntity entity,
                                                                   WorkerEntity worker, ShiftTemplateEntity shift) {
        return new ShiftAssignmentResponseDTO(
                entity.getId(),
                entity.getWorkerId(),
                worker == null ? null : worker.getEmployeeId(),
                displayName(worker),
                entity.getShiftId(),
                shift == null ? null : shift.getShiftCode(),
                shift == null ? null : shift.getShiftName(),
                shift == null ? null : shift.getStartTime(),
                shift == null ? null : shift.getEndTime(),
                shift == null ? null : shift.getTotalWorkingMinutes(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getStatus()
        );
    }

    /**
     * A worker's display name.
     *
     * @param worker the worker, or null
     * @return first and last name, or null when the worker is unknown
     */
    private String displayName(WorkerEntity worker) {
        return worker == null ? null
                : (worker.getFirstName() + " " + (worker.getLastName() == null ? "" : worker.getLastName())).trim();
    }
}

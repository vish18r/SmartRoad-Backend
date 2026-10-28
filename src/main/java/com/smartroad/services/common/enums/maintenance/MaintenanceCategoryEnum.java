package com.smartroad.services.common.enums.maintenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the sub-category of a maintenance record. Each category belongs to one
 * {@link MaintenanceTypeEnum} (preventive servicing categories vs. corrective repair categories);
 * {@code OTHER} is accepted for either type.
 */
public enum MaintenanceCategoryEnum implements EnumValueHandler {

    SCHEDULED_SERVICING("scheduled_servicing", MaintenanceTypeEnum.PREVENTIVE),
    OIL_FILTER_CHANGE("oil_filter_change", MaintenanceTypeEnum.PREVENTIVE),
    ROUTINE_INSPECTION("routine_inspection", MaintenanceTypeEnum.PREVENTIVE),
    LUBRICATION("lubrication", MaintenanceTypeEnum.PREVENTIVE),
    SCHEDULED_PART_REPLACEMENT("scheduled_part_replacement", MaintenanceTypeEnum.PREVENTIVE),
    BREAKDOWN("breakdown", MaintenanceTypeEnum.CORRECTIVE),
    EQUIPMENT_REPAIR("equipment_repair", MaintenanceTypeEnum.CORRECTIVE),
    FAULT_DIAGNOSIS("fault_diagnosis", MaintenanceTypeEnum.CORRECTIVE),
    PART_REPLACEMENT("part_replacement", MaintenanceTypeEnum.CORRECTIVE),
    EMERGENCY_MAINTENANCE("emergency_maintenance", MaintenanceTypeEnum.CORRECTIVE),
    OTHER("other", null);

    private final String value;
    private final MaintenanceTypeEnum maintenanceType;

    MaintenanceCategoryEnum(String value, MaintenanceTypeEnum maintenanceType) {
        this.value = value;
        this.maintenanceType = maintenanceType;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Returns whether this category may be used with the given maintenance type.
     *
     * @param type the maintenance type of the record
     * @return true when the category belongs to the type, or is {@code OTHER}
     */
    public boolean isAllowedFor(MaintenanceTypeEnum type) {
        return maintenanceType == null || maintenanceType == type;
    }

    @JsonCreator
    public static MaintenanceCategoryEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MaintenanceCategoryEnum.class, value);
    }
}

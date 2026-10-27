package com.smartroad.services.common.enums.resourceplan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the kind of conflict detected while planning resource allocations.
 */
public enum ConflictTypeEnum implements EnumValueHandler {

    WORKER_CONFLICT("worker_conflict"),
    MACHINE_CONFLICT("machine_conflict"),
    VEHICLE_CONFLICT("vehicle_conflict"),
    RESOURCE_AVAILABILITY("resource_availability"),
    MATERIAL_SHORTAGE("material_shortage"),
    SHIFT_CONFLICT("shift_conflict");

    private final String value;

    ConflictTypeEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConflictTypeEnum fromValue(String value) {
        return EnumValueHandler.fromValue(ConflictTypeEnum.class, value);
    }
}

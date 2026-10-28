package com.smartroad.services.common.enums.maintenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.smartroad.services.common.enums.EnumValueHandler;

/**
 * Represents the urgency of a maintenance record; for corrective (breakdown) work this doubles as the reported severity.
 */
public enum MaintenancePriorityEnum implements EnumValueHandler {

    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    CRITICAL("critical");

    private final String value;

    MaintenancePriorityEnum(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MaintenancePriorityEnum fromValue(String value) {
        return EnumValueHandler.fromValue(MaintenancePriorityEnum.class, value);
    }
}
